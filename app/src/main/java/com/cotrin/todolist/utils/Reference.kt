package com.cotrin.todolist.utils

import java.time.format.DateTimeFormatter

object Reference {
    const val TASK = "TASK"
    const val REQUEST_ID_START = "REQUEST_ID_START"

    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d日")
    val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")
    val YEAR_MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月")
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    const val APP_ID = "TODOListTasks"
    const val TASK_LIST = "TASK_LIST"

    const val ADD = "ADD"
    const val EDIT = "EDIT"

    const val POSITION = "POSITION"
}