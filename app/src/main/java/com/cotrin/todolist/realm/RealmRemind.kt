package com.cotrin.todolist.realm

import com.cotrin.todolist.ReminderInterval
import io.realm.kotlin.types.RealmObject

class RealmRemind: RealmObject {
    private var enumName: String = ReminderInterval.NONE.toString()
    var enum: ReminderInterval
        get() { return ReminderInterval.valueOf(enumName) }
        set(value) { enumName = value.name }
}