package com.cotrin.todolist.utils

import java.time.format.DateTimeFormatter

object Reference {
    const val REQUEST_ID_START = "REQUEST_ID_START"

    val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d日")
    val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    const val APP_ID = "TODOList"

    const val ADD = "ADD"
    const val EDIT = "EDIT"
}