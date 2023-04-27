package com.cotrin.todolist.taskDetailActivity

import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.R

class TaskListViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    val finishedSwitch: CheckBox = view.findViewById(R.id.finishedCheckBox)
    val taskName: TextView = view.findViewById(R.id.taskName)
    val taskTime: TextView = view.findViewById(R.id.taskTime)
    val deleteTaskButton: ImageButton = view.findViewById(R.id.deleteTaskButton)
    val remindIcon: ImageView = view.findViewById(R.id.remindIcon)
    val repeatIcon : ImageView = view.findViewById(R.id.repeatIcon)
    val carryoverIcon: ImageView = view.findViewById(R.id.carryoverIcon)
    val categoryIcon: ImageView = view.findViewById(R.id.categoryIcon)
}