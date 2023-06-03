package com.cotrin.todolist.model

import android.view.View
import com.cotrin.todolist.model.ReminderInterval.NONE
import com.cotrin.todolist.mainActivity.MainActivity
import com.cotrin.todolist.utils.GsonUtils
import com.cotrin.todolist.utils.Reference
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

data class Task(
    var name: String = "",
    var date: LocalDate = LocalDate.now(),
    var time: LocalTime? = null,
    var remind: ReminderInterval = NONE,
    var repeat: RepeatInterval = RepeatInterval.NONE,
    var carryover: Boolean = false,
    var category: TaskCategory = TaskCategory.OTHER,
    val subTasks: MutableList<SubTask> = mutableListOf(),
    var isFinished: Boolean = false,
    val uuid: UUID = UUID.randomUUID(),
    val requestID: Int = generateRequestID()
) {
    companion object {
        val generateRequestID = getRequestID()

        //シーケンシャルID生成
        private fun getRequestID(): () -> Int {
            var id = MainActivity.sharedPreferences.getInt(Reference.REQUEST_ID_START, 0)
            return {
                id++
                id
            }
        }
    }

    fun toJsonString(): String {
        val gson = GsonUtils.getCustomGson()
        return gson.toJson(this)
    }

    fun getRemindTimeInMills(): Long {
        val remindDateTime = LocalDateTime.of(date, time)
        val reminderTime = remindDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intervalMills = remind.minute * 60 * 1000
        return reminderTime - intervalMills
    }

    fun getDateText(): String {
        return date.format(Reference.MONTH_DAY_FORMATTER)
    }

    fun getTimeText(): String {
        return time?.format(Reference.TIME_FORMATTER) ?: ""
    }

    fun getVisibilityTimeText(): Int {
        return time?.let { View.VISIBLE } ?: run { View.GONE }
    }

    fun getVisibilityRemindIcon(): Int {
        return if (remind != NONE) View.VISIBLE else View.GONE
    }

    fun getVisibilityRepeatIcon(): Int {
        return if (repeat != RepeatInterval.NONE) View.VISIBLE else View.GONE
    }

    fun getVisibilityCarryoverIcon(): Int {
        return if (carryover) View.VISIBLE else View.GONE
    }
}
