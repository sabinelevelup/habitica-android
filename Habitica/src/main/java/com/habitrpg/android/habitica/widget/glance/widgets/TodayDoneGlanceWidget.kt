package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import android.graphics.Bitmap
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.SignedOutContent
import com.habitrpg.android.habitica.widget.glance.components.stringRes
import com.habitrpg.android.habitica.widget.glance.data.TodayDoneWidgetState
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.data.WidgetBackgroundCache
import com.habitrpg.android.habitica.widget.glance.data.WidgetSnapshotStore
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme

class TodayDoneGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 180.dp),
            DpSize(250.dp, 220.dp),
            DpSize(320.dp, 180.dp),
            DpSize(180.dp, 320.dp),
            DpSize(400.dp, 280.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        if (!WidgetAuth.isLoggedIn(context)) {
            provideContent { HabiticaWidgetTheme { SignedOutContent() } }
            return
        }
        val background = runCatching {
            val user = withContext(Dispatchers.Main) {
                widgetEntryPoint(context).userRepository().getUser().firstOrNull()
            }
            WidgetBackgroundCache.refreshIfNeeded(context, user?.preferences?.background)
            WidgetBackgroundCache.cachedBitmap(context)
        }.getOrNull()
        provideContent {
            val state = WidgetSnapshotStore.todayDoneFrom(currentState())
                ?: TodayDoneWidgetState(habitsDone = 0, dailiesDone = 0, todosDone = 0, needsCron = false)
            HabiticaWidgetTheme {
                TodayDoneTile(state, background)
            }
        }
    }
}

@Composable
private fun TodayDoneTile(state: TodayDoneWidgetState, background: Bitmap?) {
    val height = LocalSize.current.height.value
    val numberSize = (height / 6.2f).coerceIn(28f, 46f).sp
    val labelSize = (numberSize.value * 0.58f).coerceIn(16f, 28f).sp
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(ColorProvider(R.color.widget_today_bg_start)),
    ) {
        Image(
            provider = if (background != null) {
                ImageProvider(background)
            } else {
                ImageProvider(R.drawable.widget_today_pixel_bg)
            },
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (state.needsCron) {
            StartDayContent(labelSize)
        } else {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .clickable(onClick = openAppAction("habitica://user/tasks/daily")),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountRow(state.habitsDone, stringRes(R.string.habits), ColorProvider(R.color.widget_pixel_habit), numberSize, labelSize)
                CountRow(state.dailiesDone, stringRes(R.string.dailies), ColorProvider(R.color.widget_pixel_daily), numberSize, labelSize)
                CountRow(state.todosDone, stringRes(R.string.todos), ColorProvider(R.color.widget_pixel_todo), numberSize, labelSize)
            }
        }
    }
}

@Composable
private fun CountRow(
    count: Int,
    label: String,
    numberColor: ColorProvider,
    numberSize: androidx.compose.ui.unit.TextUnit,
    labelSize: androidx.compose.ui.unit.TextUnit,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = count.toString(),
            style = TextStyle(
                color = numberColor,
                fontSize = numberSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            ),
            modifier = GlanceModifier.width(64.dp),
        )
        Spacer(GlanceModifier.width(12.dp))
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(R.color.widget_pixel_label),
                fontSize = labelSize,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun StartDayContent(labelSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier
                .padding(16.dp)
                .clickable(onClick = openAppAction("habitica://user/tasks/daily")),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringRes(R.string.widget_start_day),
                style = TextStyle(
                    color = ColorProvider(R.color.widget_pixel_label),
                    fontSize = labelSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
