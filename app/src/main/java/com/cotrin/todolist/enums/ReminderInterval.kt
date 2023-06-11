package com.cotrin.todolist.enums

enum class ReminderInterval(val minute: Int, val optionName: String, val notifyMessage: String) {
    NONE(-1, "リマインダー無し", ""),
    EXACT_DEADLINE(0, "期限当日", "タスクの期限です。"),
    FIVE_MINUTES(5, "5分前", "タスクの期限まであと5分です"),
    FIFTEEN_MINUTES(15, "15分前", "タスクの期限まであと15分です"),
    THIRTY_MINUTES(30, "30分前", "タスクの期限まであと30分です"),
    ONE_HOUR(60, "1時間前", "タスクの期限まであと1時間です"),
    THREE_HOUR(180, "3時間前", "タスクの期限まであと3時間です"),
    ONE_DAY(1440, "1日前", "タスクの期限まであと1日です")
}