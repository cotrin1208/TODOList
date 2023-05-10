package com.cotrin.todolist.mainActivity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.R
import com.cotrin.todolist.Task
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.utils.putTask
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity(), OnDialogResultListener {
    private lateinit var taskListRecycler: RecyclerView
    private lateinit var tabLayout: TabLayout

    companion object {
        var date: LocalDate = LocalDate.now()
        lateinit var sharedPreferences: SharedPreferences
        private val dateRange = -500 .. 500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = this@MainActivity.getSharedPreferences(Reference.APP_ID, MODE_PRIVATE)
        Task.loadTasks()

        //翌日繰り越し処理
        Task.carryoverPreviousTasks(date)

        //RecyclerViewの表示
        taskListRecycler = findViewById<RecyclerView?>(R.id.taskListRecyclerView).apply {
            adapter = if (Task.taskList.containsKey(date)) {
                TaskListRecyclerAdapter(Task.taskList[date]!!)
            } else {
                TaskListRecyclerAdapter(mutableListOf())
            }
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter?.let {
                it as TaskListRecyclerAdapter
                //RecyclerView内のチェックボックスにリスナー登録、タスク保存
                it.setOnCheckBoxClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        if (Task.taskList.containsKey(date)) {
                            Task.taskList[date]!![position].isFinished = !Task.taskList[date]!![position].isFinished
                            Task.saveTasks()
                        }
                    }
                })

                //RecyclerView内の削除ボタンにリスナー登録
                it.setOnTaskDetailClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        showDeleteAlertDialog {
                            val task = Task.taskList[date]!![position]
                            Task.removeTaskByUUID(task.uuid)
                            updateDeleted(position)
                            Task.saveTasks()
                        }
                    }
                })

                //RecyclerViewの行Viewにリスナー登録
                it.setOnTaskClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val task = Task.taskList[date]!![position]
                        TaskDetailFragment().apply {
                            val args = Bundle()
                            args.putTask(Reference.TASK, task)
                            args.putInt(Reference.POSITION, position)
                            arguments = args
                        }.show(supportFragmentManager, Reference.EDIT)
                    }
                })
            }
        }

        //日付タブ
        tabLayout = findViewById<TabLayout>(R.id.dateTab).apply {
            for (i in dateRange) {
                addTab(this.newTab().setText(date.plusDays(i.toLong()).toString()))
            }

            addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    date = LocalDate.now().plusDays(tab.position - dateRange.last.toLong())
                    updateTaskList()
                    post { smoothScrollTo(tab.view.left - width / 3, 0) }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            post { getTabAt(dateRange.last)?.select() }
        }

        //タスク追加ボタン。
        val addTaskButton: FloatingActionButton = findViewById(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            TaskDetailFragment().apply {
                val args = Bundle()
                args.putTask(Reference.TASK, Task())
                arguments = args
            }.show(supportFragmentManager, Reference.ADD)
        }

        //カレンダー表示ボタン
        val showCalendarButton: FloatingActionButton = findViewById(R.id.calendarButton)
        showCalendarButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    //タスク一覧をクリックしたらカレンダーを開く
    private fun showDatePickerDialog() {
        DatePickerDialog(this).apply {
            setOnDateSetListener { _, year, month, dayOfMonth ->
                val daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.of(year, month + 1, dayOfMonth))
                tabLayout.getTabAt(daysBetween.toInt() + dateRange.last)?.select()
                date = LocalDate.of(year, month + 1, dayOfMonth)
                updateTaskList()
            }
            show()
        }
    }

    //削除ダイアログを表示
    private fun showDeleteAlertDialog(onConform: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setTitle("タスクを削除する")
            setMessage("タスクを削除しますか？繰り返し設定がされている場合,今後のタスクもすべて削除されます。")
            setPositiveButton("削除") { _, _ ->
                onConform.invoke()
            }
            setNegativeButton("キャンセル", null)
        }.create().apply {
            setOnShowListener {
                val message = this.findViewById<TextView>(android.R.id.message)
                message?.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
                getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
                getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_light))
            }
        }.show()
    }

    //RecyclerViewを更新
    private fun updateTaskList() {
        if (Task.taskList.containsKey(date)) {
            (taskListRecycler.adapter as TaskListRecyclerAdapter?)?.setTaskList(Task.taskList[date]!!)
        } else {
            (taskListRecycler.adapter as TaskListRecyclerAdapter?)?.setTaskList(mutableListOf())
        }
    }

    private fun updateAdded() {
        (taskListRecycler.adapter as TaskListRecyclerAdapter).apply {
            notifyItemInserted(itemCount)
        }
    }

    private fun updateDeleted(position: Int) {
        (taskListRecycler.adapter as TaskListRecyclerAdapter).apply {
            notifyItemRemoved(position)
        }
    }

    private fun updateEdited(position: Int) {
        (taskListRecycler.adapter as TaskListRecyclerAdapter).apply {
            notifyItemChanged(position)
        }
    }

    override fun onDialogResult(task: Task, position: Int, mode: String) {
        Task.removeTaskByUUID(task.uuid)
        Task.addTask(task, date)
        if (mode == Reference.ADD) updateAdded()
        else if (mode == Reference.EDIT) updateEdited(position)
    }
}