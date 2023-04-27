package com.cotrin.todolist

enum class ReminderInterval(val minute: Int, val OptionName: String) {
    NONE(0, "リマインダー無し"),
    FIFTEEN_MINUTES(15, "15分前"),
    THIRTY_MINUTES(30, "30分前"),
    ONE_HOUR(60, "1時間前"),
    ONE_DAY(1440, "1日前")
}