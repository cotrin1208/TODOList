package com.cotrin.todolist.mainActivity

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.*
import com.cotrin.todolist.ReminderInterval.*
import com.cotrin.todolist.notification.RemindNotificationReceiver
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.taskDetailActivity.TaskDetailActivity
import com.cotrin.todolist.taskDetailActivity.TaskListRecyclerAdapter
import com.cotrin.todolist.utils.GsonUtils
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.utils.getTaskExtra
import com.cotrin.todolist.utils.putExtra
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var addTaskLauncher: ActivityResultLauncher<Intent>
    private lateinit var editTaskLauncher: ActivityResultLauncher<Intent>
    private lateinit var textDate: TextView
    private lateinit var taskListRecycler: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        var taskList = mutableMapOf<LocalDate, MutableList<Task>>()
        var date: LocalDate = LocalDate.now()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = this@MainActivity.getSharedPreferences(Reference.APP_ID, MODE_PRIVATE)
        val requestID = getRequestID()
        taskList = loadTasks()

        //RecyclerViewの表示
        taskListRecycler = findViewById<RecyclerView?>(R.id.taskListRecyclerView).apply {
            adapter = if (taskList.containsKey(date)) {
                TaskListRecyclerAdapter(taskList[date]!!)
            } else {
                TaskListRecyclerAdapter(mutableListOf())
            }
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter?.let {
                it as TaskListRecyclerAdapter
                //RecyclerView内のチェックボックスにリスナー登録、タスク保存
                it.setOnCheckBoxClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        if (taskList.containsKey(date)) {
                            taskList[date]!![position].isFinished = !taskList[date]!![position].isFinished
                            saveTasks()
                        }
                    }
                })

                //RecyclerView内の削除ボタンにリスナー登録
                it.setOnTaskDetailClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        showDeleteAlertDialog {
                            removeTaskByUUID(taskList[date]!![position].uuid)
                            updateTaskList()
                            saveTasks()
                        }
                    }
                })

                //RecyclerViewの行Viewにリスナー登録
                it.setOnTaskClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val task = taskList[date]!![position]
                        val intent = Intent(this@MainActivity, TaskDetailActivity::class.java)
                        intent.putExtra(Reference.TASK, task)
                        intent.putExtra(Reference.TASK_POSITION, position)
                        editTaskLauncher.launch(intent)
                        removeTaskByUUID(task.uuid)
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
                //タスクが正常に受け取れた場合、タスク追加
                task = task.copy(requestID = requestID())
                addTask(task)
            }
        }

        //タスク編集用Intent処理
        editTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //TaskDetailActivityからデータを受け取る
                val data: Intent = result.data!!
                var task = data.getTaskExtra(Reference.TASK)
                //タスクが正常に受け取れた場合、タスク追加、削除
                task = task.copy(requestID = requestID())
                addTask(task)
                val position = data.getIntExtra(Reference.TASK_POSITION, -1)
                if (position != -1) removeTaskByUUID(taskList[date]!![position].uuid)
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
        if (taskList.containsKey(date)) {
            (taskListRecycler.adapter as TaskListRecyclerAdapter?)?.setTaskList(taskList[date]!!)
        } else {
            (taskListRecycler.adapter as TaskListRecyclerAdapter?)?.setTaskList(mutableListOf())
        }
    }

    private fun addTask(task: Task) {
        if (taskList.containsKey(task.date))
            taskList[task.date]?.add(task)
        else {  
            taskList[task.date] = mutableListOf(task)
        }
        val editor = sharedPreferences.edit()
        editor.putInt(Reference.REQUEST_ID_START, task.requestID)
        editor.apply()
        getRemindTime(task)?.let { setRemind(this@MainActivity, it, task.requestID) }
        updateTaskList()
        saveTasks()
    }

    private fun removeTaskByUUID(uuid: UUID) {
        taskList.forEach { (_, tasks) ->
            tasks.removeIf {
                removeRemind(this@MainActivity, it.requestID)
                it.uuid == uuid
            }
        }
    }

    private fun saveTasks() {
        val editor = sharedPreferences.edit()
        val gson = GsonUtils.getCustomGson()
        val json = gson.toJson(taskList)
        editor.putString(Reference.TASK_LIST, json)
        editor.apply()
    }

    private fun loadTasks(): MutableMap<LocalDate, MutableList<Task>> {
        val gson = GsonUtils.getCustomGson()
        val json = sharedPreferences.getString(Reference.TASK_LIST, null)
        val type = object: TypeToken<MutableMap<LocalDate, MutableList<Task>>>(){}.type
        if (json != null) return gson.fromJson(json, type) as MutableMap<LocalDate, MutableList<Task>>
        return mutableMapOf()
    }

    //通知関連
    private fun setRemind(context: Context, calendar: Calendar, requestID: Int) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, RemindNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestID, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun removeRemind(context: Context, requestID: Int) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, RemindNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestID, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun getRemindTime(task: Task): Calendar? {
        if (task.time == null) return null
        val calendar = Calendar.getInstance()
        calendar.set(task.date.year, task.date.monthValue, task.date.dayOfMonth, task.time.hour, task.time.minute)
        when (task.remindInterval)  {
            NONE -> null
            FIFTEEN_MINUTES -> calendar.add(Calendar.SECOND, 10)
            THIRTY_MINUTES -> calendar.add(Calendar.MINUTE, -30)
            ONE_HOUR -> calendar.add(Calendar.HOUR, -1)
            ONE_DAY -> calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        return calendar
    }

    private fun getRequestID(): () -> Int {
        var id = sharedPreferences.getInt(Reference.REQUEST_ID_START, 0)
        return {
            id++
            id
        }
    }
}