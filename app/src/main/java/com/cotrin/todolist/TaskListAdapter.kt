package com.cotrin.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cotrin.todolist.databinding.LayoutTaskCardBinding
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.taskDetailActivity.OnTextChangeListener
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

    inner class TaskViewHolder(private val binding: LayoutTaskCardBinding): ViewHolder(binding.root) {
        fun bind(task: Task, viewLifecycleOwner: LifecycleOwner, viewModel: TaskViewModel) {
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                this.task = task
                this.viewModel = viewModel
                executePendingBindings()
            }
            //アコーディオンの設定
            binding.expandableLayout.setExpanded(viewModel.isExpansion.value!!, false)
            //カテゴリアイコン設定
            val drawable = AppCompatResources.getDrawable(binding.root.context, task.category.iconResId)
            binding.categoryChip.chipIcon = drawable
            //サブタスク追加
            binding.addSubTask.setOnClickListener {
                task.subTasks.add(SubTask())
                binding.subTasks.adapter?.notifyItemInserted(task.subTasks.size - 1)
                viewModel.saveTasks()
            }
            binding.subTasks.adapter = SubTaskAdapter(task.subTasks).apply {
                //チェックボックスリスナー登録
                setOnCheckBoxClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        task.subTasks[position].isFinished = (view as CheckBox).isChecked
                        viewModel.saveTasks()
                    }
                })
                //テキストリスナー登録
                setOnTextChangeListener(object: OnTextChangeListener {
                    override fun onTextChanged(s: CharSequence, position: Int) {
                        task.subTasks[position].name = s.toString()
                        viewModel.saveTasks()
                    }
                })
                //サブタスク削除ボタンのリスナー登録
                setOnDeleteButtonClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        task.subTasks.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, itemCount)
                        viewModel.saveTasks()
                    }
                })
            }
        }
    }
}