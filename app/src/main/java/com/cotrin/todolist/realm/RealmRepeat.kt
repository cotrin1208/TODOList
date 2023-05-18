package com.cotrin.todolist.realm

import com.cotrin.todolist.RepeatInterval
import io.realm.kotlin.types.RealmObject

class RealmRepeat: RealmObject {
    private var enumName: String = RepeatInterval.NONE.toString()
    var enum: RepeatInterval
        get() { return RepeatInterval.valueOf(enumName) }
        set(value) { enumName = value.name }
}