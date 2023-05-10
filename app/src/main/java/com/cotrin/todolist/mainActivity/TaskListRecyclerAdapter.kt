package com.cotrin.todolist.mainActivity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.*
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.utils.Reference

class TaskListRecyclerAdapter(private var taskList: MutableList<Task>): RecyclerView.Adapter<TaskListViewHolder>() {
    private lateinit var checkBoxClickListener: OnItemClickListener
    private lateinit var deleteTaskButtonClickListener: OnItemClickListener
    private lateinit var taskClickListener: OnItemClickListener

    //1行分のレイアウトを作成。task_layout.xmlとTaskListViewHolderを紐つける
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        //レイアウトを取得(インフレート)
        val taskXml = LayoutInflater.from(parent.context).inflate(R.layout.task_card_layout, parent, false)
        val anim = AlphaAnimation(0f, 1f)
        anim.startOffset = 200L
        anim.duration = 500L
        taskXml.startAnimation(anim)
        return TaskListViewHolder(taskXml)
    }

    //position番目のデータをレイアウトにセット
    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
        //チェックボックスの状態を設定
        holder.finishedSwitch.isChecked = taskList[position].isFinished
        //チェックボックスのリスナーを設定
        holder.finishedSwitch.setOnClickListener {
            checkBoxClickListener.onItemClick(it, position)
        }
        holder.deleteTaskButton.setOnClickListener {
            deleteTaskButtonClickListener.onItemClick(it, position)
        }
        holder.view.setOnClickListener {
            taskClickListener.onItemClick(it, position)
        }
        //タスク名を設定
        holder.taskName.text = taskList[position].name
        //時間を設定
        taskList[position].time?.let {
            holder.taskTime.text = it.format(Reference.TIME_FORMATTER)
            holder.taskTime.visibility = View.VISIBLE
        } ?: run {
            holder.taskTime.visibility = View.GONE
        }
        //リマインドアイコンの設定
        if (taskList[position].remindInterval != ReminderInterval.NONE) {
            holder.remindIcon.visibility = View.VISIBLE
        } else holder.remindIcon.visibility = View.GONE
        //リピートアイコンの設定
        if (taskList[position].repeatInterval != RepeatInterval.NONE) {
            holder.repeatIcon.visibility = View.VISIBLE
        } else holder.repeatIcon.visibility = View.GONE
        //翌日持ち越しアイコンの設定
        if (taskList[position].carryover) {
            holder.carryoverIcon.visibility = View.VISIBLE
        } else holder.carryoverIcon.visibility = View.GONE
        //カテゴリアイコンの設定
        val drawable = ContextCompat.getDrawable(holder.view.context, taskList[position].category.iconResId)
        holder.categoryIcon.setImageDrawable(drawable)
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
        this.deleteTaskButtonClickListener = listener
    }

    fun setOnTaskClickListener(listener: OnItemClickListener) {
        this.taskClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTaskList(list: MutableList<Task>) {
        taskList = list
        this.notifyDataSetChanged()
    }
}