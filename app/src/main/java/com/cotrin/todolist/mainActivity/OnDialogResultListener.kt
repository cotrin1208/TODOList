package com.cotrin.todolist.mainActivity

import com.cotrin.todolist.Task

interface OnDialogResultListener {
    fun onDialogResult(task: Task, position: Int, mode: String)
}