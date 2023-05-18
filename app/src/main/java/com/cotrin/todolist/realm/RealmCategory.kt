package com.cotrin.todolist.realm

import com.cotrin.todolist.TaskCategory
import io.realm.kotlin.types.RealmObject

class RealmCategory : RealmObject {
    private var enumName: String = TaskCategory.OTHER.toString()
    var enum: TaskCategory
        get() { return TaskCategory.valueOf(enumName) }
        set(value) { enumName = value.name }
}