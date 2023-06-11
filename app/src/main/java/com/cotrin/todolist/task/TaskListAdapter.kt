package com.cotrin.todolist.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cotrin.todolist.databinding.LayoutTaskCardBinding
import com.cotrin.todolist.realm.Task
import com.cotrin.todolist.realm.TaskViewModel

class TaskListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: TaskViewModel
    ): ListAdapter<Task, TaskListAdapter.TaskViewHolder>(DiffCallBack) {
    companion object {
        //差分コールバックをprivateで定義
        private val DiffCallBack = object: DiffUtil.ItemCallback<Task>() {
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
                        oldItem.category == newItem.category &&
                        oldItem.subTasks.map { it.isFinished } == newItem.subTasks.map { it.isFinished } &&
                        oldItem.subTasks.map { it.name } == newItem.subTasks.map { it.name } &&
                        oldItem.uuid == newItem.uuid &&
                        oldItem.requestID == newItem.requestID)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(LayoutTaskCardBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position), viewLifecycleOwner, viewModel)
    }

    inner class TaskViewHolder(private val binding: LayoutTaskCardBinding): ViewHolder(binding.root) {
        fun bind(task: Task, viewLifecycleOwner: LifecycleOwner, viewModel: TaskViewModel) {
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                this.task = task
                this.viewModel = viewModel
                executePendingBindings()
            }

            val progressBar = binding.progressBar
            progressBar.maxValue = if (task.subTasks.size == 0) 1f else task.subTasks.size.toFloat()
            progressBar.setValueAnimated(task.subTasks.count { it.isFinished }.toFloat(), 200)

            fun updateProgress() {
                progressBar.maxValue = if (task.subTasks.size == 0) 1.1f else task.subTasks.size.toFloat()
                progressBar.setValueAnimated(task.subTasks.count { it.isFinished }.toFloat(), 200)
            }
            updateProgress()

            //アコーディオンの設定
            binding.expandableLayout.setExpanded(viewModel.isExpansion.value!!, false)
            //カテゴリアイコン設定
            val drawable = AppCompatResources.getDrawable(binding.root.context, task.category.iconResId)
            binding.categoryChip.chipIcon = drawable
            //サブタスク追加
            binding.addSubTask.setOnClickListener {
                viewModel.addSubTask(task)
            }
            //サブタスクRecyclerViewのリスト設定
            binding.subTasks.adapter = SubTaskListAdapter(viewLifecycleOwner, viewModel, task)
            (binding.subTasks.adapter as SubTaskListAdapter).submitList(task.subTasks)
        }
    }
}