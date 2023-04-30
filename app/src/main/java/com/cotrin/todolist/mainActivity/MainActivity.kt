package com.cotrin.todolist.mainActivity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.DateChangeReceiver
import com.cotrin.todolist.R
import com.cotrin.todolist.Task
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.taskDetailActivity.TaskDetailActivity
import com.cotrin.todolist.taskDetailActivity.TaskListRecyclerAdapter
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.utils.getTaskExtra
import com.cotrin.todolist.utils.putExtra
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var addTaskLauncher: ActivityResultLauncher<Intent>
    private lateinit var textDate: TextView
    private lateinit var taskListRecycler: RecyclerView

    companion object {
        var date: LocalDate = LocalDate.now()
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = this@MainActivity.getSharedPreferences(Reference.APP_ID, MODE_PRIVATE)
        Task.loadTasks()

        //翌日繰り越し処理のためのBroadcastReceiverを登録する
        val receiver = DateChangeReceiver()
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
        }
        registerReceiver(receiver, intentFilter)

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
                            updateTaskList()
                            Task.saveTasks()
                        }
                    }
                })

                //RecyclerViewの行Viewにリスナー登録
                it.setOnTaskClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val task = Task.taskList[date]!![position]
                        val intent = Intent(this@MainActivity, TaskDetailActivity::class.java)
                        intent.putExtra(Reference.TASK, task)
                        addTaskLauncher.launch(intent)
                        updateTaskList()
                    }
                })
            }
        }

        //タスク追加用Intent処理
        addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //TaskDetailActivityからデータを受け取る
                val data: Intent = result.data!!
                var task = data.getTaskExtra(Reference.TASK)
                //タスクが正常に受け取れた場合、重複するUUIDを持つタスクを削除後、タスク追加
                task = task.copy()
                Task.removeTaskByUUID(task.uuid)
                Task.addTask(task, date)
                updateTaskList()
            }
        }

        //タスク一覧テキスト。カレンダーを表示
        val titleText: TextView = findViewById(R.id.textViewTitle)
        titleText.setOnClickListener {
            showDatePickerDialog()
        }

        //タスク追加ボタン。アクティビティを重ねて表示
        val addTaskButton: FloatingActionButton = findViewById(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            val intent = Intent(this, TaskDetailActivity::class.java)
            addTaskLauncher.launch(intent)
        }

        //日付表示
        textDate = findViewById(R.id.textViewTaskDate)
        textDate.text = date.format(Reference.DATE_FORMATTER)

        //翌日へ移動
        val nextDayButton: ImageView = findViewById(R.id.nextDayButton)
        nextDayButton.setOnClickListener {
            val fadeOut = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_out)
            textDate.startAnimation(fadeOut)
            date = date.plusDays(1)
            textDate.text = date.format(Reference.DATE_FORMATTER)
            val fadeIn = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_in)
            textDate.startAnimation(fadeIn)
            updateTaskList()
        }

        //前日へ移動
        val previousDayButton: ImageView = findViewById(R.id.previousDayButton)
        previousDayButton.setOnClickListener {
            val fadeOut = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_out)
            textDate.startAnimation(fadeOut)
            date = date.plusDays(-1)
            textDate.text = date.format(Reference.DATE_FORMATTER)
            val fadeIn = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_in)
            textDate.startAnimation(fadeIn)
            updateTaskList()
        }
    }

    //タスク一覧をクリックしたらカレンダーを開く
    private fun showDatePickerDialog() {
        DatePickerDialog(this).apply {
            setOnDateSetListener { _, year, month, dayOfMonth ->
                date = LocalDate.of(year, month + 1, dayOfMonth)
                textDate.text = date.format(Reference.DATE_FORMATTER)
                updateTaskList()
            }
            show()
        }
    }

    //削除ダイアログを表示
    private fun showDeleteAlertDialog(onConform: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setTitle("タスクを削除する")
            setMessage("タスクを削除しますか？繰り返し設定がされている場合今後のタスクもすべて削除されます。")
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
}