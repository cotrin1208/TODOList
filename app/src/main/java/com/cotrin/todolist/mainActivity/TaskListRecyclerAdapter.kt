package com.cotrin.todolist.mainActivity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.ReminderInterval.NONE
import com.cotrin.todolist.RepeatInterval
import com.cotrin.todolist.SubTask
import com.cotrin.todolist.SubTaskAdapter
import com.cotrin.todolist.Task
import com.cotrin.todolist.databinding.LayoutTaskCardBinding
import com.cotrin.todolist.taskDetailActivity.OnCardClickListener
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.taskDetailActivity.OnTextChangeListener
import com.cotrin.todolist.utils.Reference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskListRecyclerAdapter : RecyclerView.Adapter<TaskListRecyclerAdapter.TaskListViewHolder>() {
    private lateinit var checkBoxClickListener: OnItemClickListener
    private lateinit var taskMenuClickListener: OnItemClickListener
    private lateinit var taskClickListener: OnCardClickListener
    private lateinit var progressChangedListener: OnItemClickListener

    //1行分のレイアウトを作成。task_layout.xmlとTaskListViewHolderを紐つける
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        //レイアウトを取得(インフレート)
        val taskXml = LayoutTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val anim = AlphaAnimation(0f, 1f)
        anim.startOffset = 200L
        anim.duration = 500L
        taskXml.root.startAnimation(anim)
        return TaskListViewHolder(taskXml)
    }

    //position番目のデータをレイアウトにセット
    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
        val task = Task.taskList[position]

        holder.bind(task)
        //プログレスバーアップデート
        fun updateProgress() {
            val progressBar = holder.binding.progressBar
            progressBar.maxValue = if (Task.taskList[position].subTasks.size == 0) {
                1f
            } else {
                Task.taskList[position].subTasks.size.toFloat()
            }
            progressBar.setValueAnimated(Task.taskList[position].subTasks.count { it.isFinished }.toFloat(), 200L)
            CoroutineScope(Dispatchers.Main).launch {
                if (task.subTasks.isNotEmpty()) {
                    delay(250)
                    val isFinish = progressBar.maxValue == progressBar.currentValue
                    holder.binding.finishedCheckBox.isChecked = isFinish
                    progressChangedListener.onItemClick(progressBar, position)
                }
            }
        }
        //チェックボックスの状態を設定
        holder.binding.finishedCheckBox.apply {
            isChecked = task.isFinished
            setOnClickListener { view ->
                checkBoxClickListener.onItemClick(view, position)
                holder.binding.subTasks.adapter?.let {
                    for (index in 0 until task.subTasks.size) {
                        task.subTasks[index] = task.subTasks[index].copy(isFinished = isChecked)
                    }
                    it.notifyItemRangeChanged(0, it.itemCount)
                    updateProgress()
                }
            }
        }
        //メニューボタンの設定
        holder.binding.taskMenuButton.setOnClickListener {
            taskMenuClickListener.onItemClick(it, position)
        }
        //タスク名を設定
        holder.binding.taskName.text = task.name
        //日付を設定
        holder.binding.taskDate.apply {
            text = task.date.format(Reference.MONTH_DAY_FORMATTER)
            if (task.date.isBefore(LocalDate.now())) { setTextColor(Color.RED) }
        }
        //時間を設定
        task.time?.let {
            holder.binding.taskTime.text = it.format(Reference.TIME_FORMATTER)
            holder.binding.taskTime.visibility = View.VISIBLE
        } ?: run {
            holder.binding.taskTime.visibility = View.GONE
        }
        //カテゴリアイコンの設定
        val drawable = ContextCompat.getDrawable(holder.binding.root.context, task.category.iconResId)
        holder.binding.categoryChip.chipIcon = drawable
        //リマインドアイコンの設定
        if (task.remind == NONE)
            holder.binding.remindChip.visibility = View.GONE
        else
            holder.binding.remindChip.text = task.remind.OptionName
        //リピートアイコンの設定
        if (task.repeat == RepeatInterval.NONE)
            holder.binding.repeatChip.visibility = View.GONE
        else
            holder.binding.repeatChip.text = task.repeat.OptionName
        //翌日持ち越しアイコンの設定
        if (task.carryover)
            holder.binding.carryoverChip.visibility = View.VISIBLE
        else
            holder.binding.carryoverChip.visibility = View.GONE
        //タスクカードのリスナー設定
        holder.binding.root.setOnClickListener {
            taskClickListener.onItemClick(holder.binding.expandableLayout, position)
        }
        //プログレスバーの設定
        updateProgress()
        //サブタスク設定
        holder.binding.subTasks.apply {
            layoutManager = LinearLayoutManager(holder.binding.root.context)
            adapter = SubTaskAdapter(task.subTasks)
            val subTaskAdapter = adapter as SubTaskAdapter
            //サブタスクチェックボックスのリスナー登録
            subTaskAdapter.setOnCheckBoxClickListener(object: OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val subTask = task.subTasks[position]
                    task.subTasks[position] = subTask.copy(isFinished = !subTask.isFinished)
                    Task.saveTasks()
                    updateProgress()
                }
            })
            //サブタスク名のリスナー登録
            subTaskAdapter.setOnTextChangeListener(object: OnTextChangeListener {
                override fun onTextChanged(s: CharSequence, position: Int) {
                    val subTask = task.subTasks[position]
                    task.subTasks[position] = subTask.copy(name = s.toString())
                    Task.saveTasks()
                }
            })
            //サブタスク削除ボタンのリスナー登録
            subTaskAdapter.setOnDeleteButtonClickListener(object: OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    task.subTasks.removeAt(position)
                    adapter?.let { subTaskAdapter ->
                        subTaskAdapter.notifyItemRemoved(position)
                        subTaskAdapter.notifyItemRangeChanged(position, subTaskAdapter.itemCount)
                    }
                    updateProgress()
                    Task.saveTasks()
                }
            })
        }

        //サブタスク追加ボタン
        holder.binding.addSubTask.setOnClickListener {
            task.subTasks.add(SubTask())
            holder.binding.subTasks.adapter?.notifyItemInserted(task.subTasks.size - 1)
            updateProgress()
        }
        //アニメーション
        holder.startFadeInAnimation(position)
    }

    //データの総数をカウントする
    override fun getItemCount(): Int {
        return Task.taskList.size
    }

    fun setOnCheckBoxClickListener(listener: OnItemClickListener) {
        this.checkBoxClickListener = listener
    }

    fun setOnTaskDetailClickListener(listener: OnItemClickListener) {
        this.taskMenuClickListener = listener
    }

    fun setOnTaskClickListener(listener: OnCardClickListener) {
        this.taskClickListener = listener
    }

    fun setProgressChangeListener(listener: OnItemClickListener) {
        this.progressChangedListener = listener
    }

    inner class TaskListViewHolder(val binding: LayoutTaskCardBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.task = task
        }

        fun startFadeInAnimation(position: Int) {
            val anim = AlphaAnimation(0f, 1f)
            anim.startOffset = position * 100L
            anim.duration = 500L
            itemView.startAnimation(anim)
        }
    }
}