package com.cotrin.todolist.taskDetailActivity

import android.view.View

interface OnItemClickListener {
    fun onItemClick(view: View, position: Int)
}