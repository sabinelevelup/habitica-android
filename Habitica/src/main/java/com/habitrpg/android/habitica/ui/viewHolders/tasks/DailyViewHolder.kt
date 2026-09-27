package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.GroupPlanInfoProvider
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.common.habitica.helpers.LanguageHelper
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import java.text.DateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

class DailyViewHolder(
    itemView: View,
    scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    scoreChecklistItemFunc: ((Task, ChecklistItem) -> Unit),
    openTaskFunc: ((Task, View) -> Unit),
    brokenTaskFunc: ((Task) -> Unit),
    assignedTextProvider: GroupPlanInfoProvider?
) : ChecklistedViewHolder(itemView, scoreTaskFunc, scoreChecklistItemFunc, openTaskFunc, brokenTaskFunc, assignedTextProvider) {
    override val taskIconWrapperIsVisible: Boolean
        get() {
            var isVisible: Boolean = super.taskIconWrapperIsVisible
            if (this.streakTextView.visibility == View.VISIBLE) {
                isVisible = true
            }
            return isVisible
        }

    override fun bind(
        data: Task,
        position: Int,
        displayMode: String,
        ownerID: String?
    ) {
        this.task = data
        setChecklistIndicatorBackgroundActive(data.isChecklistDisplayActive)

        if (data.reminders?.size == 0) {
            reminderTextView.visibility = View.GONE
        } else {
            reminderTextView.visibility = View.VISIBLE
            val now = Date()
            val calendar = Calendar.getInstance()
            val nextReminder =
                data.reminders?.firstOrNull {
                    calendar.time = now
                    calendar.set(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE),
                        it.getZonedDateTime()?.hour ?: 0,
                        it.getZonedDateTime()?.minute ?: 0,
                        0
                    )
                    now < calendar.time
                } ?: data.reminders?.first()

            var reminderString = ""
            if (nextReminder?.time != null) {
                val time = Date.from(nextReminder.getLocalZonedDateTimeInstant())
                reminderString += formatter.format(time)
            }
            if ((data.reminders?.size ?: 0) > 1) {
                reminderString = "$reminderString (+${(data.reminders?.size ?: 0) - 1})"
            }
            reminderTextView.text = reminderString
        }

        super.bind(data, position, displayMode, ownerID)
    }

    override fun shouldDisplayAsActive(
        task: Task?,
        userID: String?
    ): Boolean {
        return task?.isDisplayedActiveForUser(userID) ?: false
    }

    override fun configureSpecialTaskTextView(task: Task) {
        super.configureSpecialTaskTextView(task)
        val lastCompleted = task.effectiveLastCompletedAt(null)
        if (lastCompleted != null && !task.isGroupTask) {
            this.streakTextView.text = formatLastCompleted(lastCompleted)
            this.streakTextView.visibility = View.VISIBLE
            this.streakIconView.visibility = View.GONE
        } else {
            this.streakTextView.visibility = View.GONE
            this.streakIconView.visibility = View.GONE
        }
    }

    private fun formatLastCompleted(date: Date): String {
        val days =
            ChronoUnit.DAYS.between(
                date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                LocalDate.now()
            ).toInt().coerceAtLeast(0)
        return when {
            days <= 30 ->
                itemView.resources.getQuantityString(R.plurals.x_days, days, days)

            days <= 90 -> {
                val weeks = (days / 7).coerceAtLeast(1)
                itemView.resources.getQuantityString(R.plurals.last_completed_weeks, weeks, weeks)
            }

            else -> {
                val months = (days / 30).coerceAtLeast(1)
                itemView.resources.getQuantityString(R.plurals.last_completed_months, months, months)
            }
        }
    }

    companion object {
        private val formatter: DateFormat
            get() {
                return DateFormat.getTimeInstance(DateFormat.SHORT, LanguageHelper.systemLocale)
            }
    }
}
