package com.cotrin.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cotrin.todolist.databinding.LayoutTaskCardBinding
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.taskDetailActivity.OnTextChangeListener
import com.cotrin.todolist.viewModel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                return oldItem == newItem
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
                progressBar.maxValue = if (task.subTasks.size == 0) 1f else task.subTasks.size.toFloat()
                progressBar.setValueAnimated(task.subTasks.count { it.isFinished }.toFloat(), 200)

                CoroutineScope(Dispatchers.Main).launch {
                    //アニメーションが終わるまで待つ
                    delay(250)
                    //サブタスクが無い場合はブロックを抜ける
                    if (task.subTasks.isEmpty()) return@launch
                    //プログレスバーが100%になったとき
                    if (progressBar.maxValue == progressBar.currentValue) {
                        //タスクを完了する
                        if (!task.isFinished) viewModel.editTask(task.copy(isFinished = true))
                        //プログレスバーが100%から変動したとき
                    } else {
                        //タスクを未達にする
                        if (task.isFinished) viewModel.editTask(task.copy(isFinished = false))
                    }
                }
            }
            updateProgress()

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
                updateProgress()
            }
            binding.subTasks.adapter = SubTaskAdapter(task.subTasks).apply {
                //チェックボックスリスナー登録
                setOnCheckBoxClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        task.subTasks[position].isFinished = (view as CheckBox).isChecked
                        viewModel.saveTasks()
                        updateProgress()
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
                        updateProgress()
                    }
                })
            }
        }
    }
}