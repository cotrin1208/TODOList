package com.cotrin.todolist.mainActivity

import com.cotrin.todolist.model.Task

interface OnDialogResultListener {
    fun onDialogResult(task: Task, mode: String)
}