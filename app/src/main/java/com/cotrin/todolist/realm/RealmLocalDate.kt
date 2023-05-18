package com.cotrin.todolist.realm

import io.realm.kotlin.types.RealmObject
import java.time.LocalDate

class RealmLocalDate: RealmObject {
    private var epochDay: Long = LocalDate.now().toEpochDay()
    var date: LocalDate
        get() { return LocalDate.ofEpochDay(epochDay) }
        set(value) { epochDay = value.toEpochDay() }
}