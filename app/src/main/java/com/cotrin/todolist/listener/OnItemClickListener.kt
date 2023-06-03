package com.cotrin.todolist.listener

import android.view.View

interface OnItemClickListener {
    fun onItemClick(view: View, position: Int)
}