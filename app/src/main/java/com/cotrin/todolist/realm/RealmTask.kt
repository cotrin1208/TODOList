package com.cotrin.todolist.realm

import com.cotrin.todolist.model.Task
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

data class RealmTask(
    val name: String,
    @PrimaryKey
    val uuid: RealmUUID = RealmUUID.random()
    ): RealmObject {
    fun convertTask(): Task {
        return Task(name = name, uuid =  UUID.fromString(uuid.toString()))
    }
}