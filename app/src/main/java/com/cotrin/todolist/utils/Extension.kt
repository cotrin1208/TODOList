package com.cotrin.todolist.utils

import java.time.LocalDate

fun LocalDate.getDayOfWeekText(): String {
    val dateText = this.format(Reference.DAY_FORMATTER)
    val dayOfWeek = when (this.dayOfWeek.value) {
        1 -> "月"
        2 -> "火"
        3 -> "水"
        4 -> "木"
        5 -> "金"
        6 -> "土"
        7 -> "日"
        else -> ""
    }
    return "$dateText ($dayOfWeek)"
}