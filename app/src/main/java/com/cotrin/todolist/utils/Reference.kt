package com.cotrin.todolist.utils

import java.time.format.DateTimeFormatter

object Reference {
    const val TASK = "TASK"
    const val TASK_POSITION = "TASK_POSITION"
    const val REQUEST_ID_START = "REQUEST_ID_START"

    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    const val APP_ID = "TODOListTasks"
    const val TASK_LIST = "TASK_LIST"
}