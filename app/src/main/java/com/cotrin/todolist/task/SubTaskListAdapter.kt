package com.cotrin.todolist.task

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cotrin.todolist.databinding.LayoutSubTaskBinding
import com.cotrin.todolist.realm.SubTask
import com.cotrin.todolist.realm.Task
import com.cotrin.todolist.realm.TaskViewModel

class SubTaskListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: TaskViewModel,
    private val parentTask: Task
    ): ListAdapter<SubTask, SubTaskListAdapter.SubTaskListViewHolder>(DiffCallBack) {
    companion object {
        val DiffCallBack = object : DiffUtil.ItemCallback<SubTask>() {
            override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
                return (oldItem.isFinished == newItem.isFinished &&
                        oldItem.name == newItem.name &&
                        oldItem.uuid == newItem.uuid)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SubTaskListViewHolder(LayoutSubTaskBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: SubTaskListViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class SubTaskListViewHolder(private val binding: LayoutSubTaskBinding): ViewHolder(binding.root) {
        fun bind(subTask: SubTask, position: Int) {
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                this.viewModel = viewModel
                this.subTask = subTask
            }

            binding.subTaskDeleteButton.setOnClickListener {
                viewModel.deleteSubTask(parentTask, position)
            }

            binding.subTaskFinishedCheckBox.setOnClickListener {
                viewModel.toggleSubTask(parentTask, position)
            }

            binding.subTaskTitleText.setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.editSubTaskName(parentTask, position, view.text.toString())
                    val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    true
                } else {
                    false
                }
            }
        }
    }
}