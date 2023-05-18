package com.cotrin.todolist.realm

import io.realm.kotlin.types.RealmObject
import java.time.LocalTime

class RealmLocalTime : RealmObject {
    private var timeString: String = LocalTime.now().toString()
    var time: LocalTime
        get() { return LocalTime.parse(timeString) }
        set(value) { timeString = value.toString() }
}