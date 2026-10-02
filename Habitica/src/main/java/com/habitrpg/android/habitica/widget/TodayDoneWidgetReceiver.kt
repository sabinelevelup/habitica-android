package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.habitrpg.android.habitica.widget.glance.widgets.TodayDoneGlanceWidget
import com.habitrpg.android.habitica.widget.glance.work.CronBoundaryRefreshWorker

class TodayDoneWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayDoneGlanceWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CronBoundaryRefreshWorker.scheduleFromCacheAsync(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        CronBoundaryRefreshWorker.scheduleFromCacheAsync(context)
    }
}
