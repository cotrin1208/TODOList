package com.cotrin.todolist

import com.cotrin.todolist.utils.GsonUtils
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Task(
    val name: String,
    val date: LocalDate,
    val time: LocalTime?,
    val remindInterval: ReminderInterval = ReminderInterval.NONE,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val carryOver: Boolean = false,
    var isFinished: Boolean = false,
    val uuid: UUID,
    val category: TaskCategory = TaskCategory.OTHER,
    val requestID: Int
) {
    fun toJsonString(): String {
        val gson = GsonUtils.getCustomGson()
        return gson.toJson(this)
    }
}