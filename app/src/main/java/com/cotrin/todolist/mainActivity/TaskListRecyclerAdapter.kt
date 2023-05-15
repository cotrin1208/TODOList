package com.cotrin.todolist.mainActivity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.*
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

class TaskListRecyclerAdapter(private var taskList: MutableList<Task>): RecyclerView.Adapter<TaskListViewHolder>() {
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
        val task = taskList[position]
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
                }
            }
        }
        //プログレスバーアップデート
        val updateProgress = {
            val progressBar = holder.binding.progressBar
            progressBar.maxValue = if (taskList[position].subTasks.size == 0) {
                1f
            } else {
                taskList[position].subTasks.size.toFloat()
            }
            progressBar.setValue(taskList[position].subTasks.count { it.isFinished }.toFloat())
            CoroutineScope(Dispatchers.Main).launch {
                delay(30)
                val isFinish = (progressBar.maxValue == progressBar.currentValue) && (progressBar.maxValue != 0f)
                holder.binding.finishedCheckBox.isChecked = isFinish
                progressChangedListener.onItemClick(progressBar, position)
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
        //リマインドアイコンの設定
        if (task.remindInterval != ReminderInterval.NONE) {
            holder.binding.remindIcon.visibility = View.VISIBLE
        } else holder.binding.remindIcon.visibility = View.GONE
        //リピートアイコンの設定
        if (task.repeatInterval != RepeatInterval.NONE) {
            holder.binding.repeatIcon.visibility = View.VISIBLE
        } else holder.binding.repeatIcon.visibility = View.GONE
        //翌日持ち越しアイコンの設定
        if (task.carryover) {
            holder.binding.carryoverIcon.visibility = View.VISIBLE
        } else holder.binding.carryoverIcon.visibility = View.GONE
        //カテゴリアイコンの設定
        val drawable = ContextCompat.getDrawable(holder.binding.root.context, task.category.iconResId)
        holder.binding.categoryIcon.setImageDrawable(drawable)
        //タスクカードのリスナー設定
        holder.binding.root.setOnClickListener {
            taskClickListener.onItemClick(holder.binding.expandableLayout, position)
        }
        //プログレスバーの設定
        updateProgress()
        //サブタスク設定
        holder.binding.subTasks.apply {
            adapter = SubTaskAdapter(task.subTasks)
            layoutManager = LinearLayoutManager(holder.binding.root.context)
            adapter?.let {
                it as SubTaskAdapter
                //サブタスクチェックボックスのリスナー登録
                it.setOnCheckBoxClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val subTask = task.subTasks[position]
                        task.subTasks[position] = subTask.copy(isFinished = !subTask.isFinished)
                        Task.saveTasks()
                        updateProgress()
                    }
                })
                //サブタスク名のリスナー登録
                it.setOnTextChangeListener(object: OnTextChangeListener {
                    override fun onTextChanged(s: CharSequence, position: Int) {
                        val subTask = task.subTasks[position]
                        task.subTasks[position] = subTask.copy(name = s.toString())
                        Task.saveTasks()
                    }
                })
                //サブタスク削除ボタンのリスナー登録
                it.setOnDeleteButtonClickListener(object: OnItemClickListener {
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
        return taskList.size
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

    @SuppressLint("NotifyDataSetChanged")
    fun setTaskList(list: MutableList<Task>) {
        taskList = list
        this.notifyDataSetChanged()
    }
}