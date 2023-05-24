package com.cotrin.todolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.databinding.LayoutTaskCardBinding
import com.cotrin.todolist.viewModel.TaskViewModel

class TaskListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: TaskViewModel
    ): ListAdapter<Task, TaskListAdapter.TaskViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(LayoutTaskCardBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position), viewLifecycleOwner, viewModel)
    }

    inner class TaskViewHolder(private val binding: LayoutTaskCardBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, viewLifecycleOwner: LifecycleOwner, viewModel: TaskViewModel) {
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                this.task = task
                this.viewModel = viewModel
                executePendingBindings()
            }
        }
    }
}