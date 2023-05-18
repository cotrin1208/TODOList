package com.cotrin.todolist.utils

import android.os.Bundle
import com.cotrin.todolist.Task
import io.realm.kotlin.types.RealmObject
import java.time.LocalDate
import java.time.LocalTime

fun Bundle.putTask(name: String, task: Task) {
    this.putString(name, task.toJsonString())
}

fun Bundle.getTask(name: String): Task {
    val json = this.getString(name, Task().toJsonString())
    val gson = GsonUtils.getCustomGson()
    return gson.fromJson(json, Task::class.java)
}