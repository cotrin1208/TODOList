package com.cotrin.todolist.utils

import android.content.Intent
import com.cotrin.todolist.Task

fun Intent.putExtra(name: String, task: Task) {
    this.putExtra(name,  task.toJsonString())
}

fun Intent.getTaskExtra(name: String): Task {
    val json = this.getStringExtra(name)
    val gson = GsonUtils.getCustomGson()
    return gson.fromJson(json, Task::class.java)
}