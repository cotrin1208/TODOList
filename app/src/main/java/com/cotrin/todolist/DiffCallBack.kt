package com.cotrin.todolist

import androidx.recyclerview.widget.DiffUtil

object DiffCallBack: DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return (oldItem.name == newItem.name &&
                oldItem.date == newItem.date &&
                oldItem.time == newItem.time &&
                oldItem.remind == newItem.remind &&
                oldItem.repeat == newItem.repeat &&
                oldItem.carryover == newItem.carryover &&
                oldItem.category == newItem.category)
    }
}