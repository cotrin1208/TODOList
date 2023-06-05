package com.cotrin.todolist.listener

import com.cotrin.todolist.model.Task

interface OnDialogResultListener {
    fun onDialogResult(task: Task, mode: String)
}