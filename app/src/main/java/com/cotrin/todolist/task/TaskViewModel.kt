package com.cotrin.todolist.task

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.cotrin.todolist.R
import com.cotrin.todolist.mainActivity.MainActivity
import com.cotrin.todolist.model.ReminderInterval
import com.cotrin.todolist.model.RepeatInterval.DAILY
import com.cotrin.todolist.model.RepeatInterval.MONTHLY
import com.cotrin.todolist.model.RepeatInterval.NONE
import com.cotrin.todolist.model.RepeatInterval.WEEKLY
import com.cotrin.todolist.model.Task
import com.cotrin.todolist.notification.TaskReminderReceiver
import com.cotrin.todolist.utils.GsonUtils
import com.cotrin.todolist.utils.Reference
import com.google.gson.reflect.TypeToken
import net.cachapa.expandablelayout.ExpandableLayout
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class TaskViewModel(application: Application): AndroidViewModel(application) {
    val taskList = MutableLiveData<List<Task>>(listOf())
    val isDetailShown = MutableLiveData(false)
    val taskDetail = MutableLiveData<Pair<View, Task>>()
    val isExpansion = MutableLiveData(false)

    fun addTask(task: Task) {
        val updatedList = taskList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(task)
        setRemindNotification(task)
        taskList.value = updatedList
    }

    fun deleteTask(task: Task) {
        val updatedList = taskList.value?.toMutableList() ?: return
        updatedList.remove(task)
        cancelRemindNotification(task)
        taskList.value = updatedList
    }

    fun editTask(task: Task) {
        val updatedList = taskList.value?.toMutableList() ?: return
        cancelRemindNotification(task)
        val index = updatedList.indexOfFirst { it.uuid == task.uuid }
        if (index != -1) {
            updatedList[index] = task.copy(uuid = UUID.randomUUID(), requestID = Task.generateRequestID())
            setRemindNotification(updatedList[index])
            taskList.value = updatedList
        }
        saveTasks()
    }

    fun copyTask(task: Task) {
        val copyTask = task.copy(uuid = UUID.randomUUID(), requestID = Task.generateRequestID())
        addTask(copyTask)
    }

    fun saveTasks() {
        val editor = MainActivity.sharedPreferences.edit()
        val gson = GsonUtils.getCustomGson()
        val json = gson.toJson(taskList.value)
        editor.putString(Reference.TASK_LIST, json)
        editor.apply()
    }

    fun loadTasks() {
        val gson = GsonUtils.getCustomGson()
        val json = MainActivity.sharedPreferences.getString(Reference.TASK_LIST, null)
        val type = object: TypeToken<List<Task>>(){}.type
        taskList.value = if (json != null) gson.fromJson(json, type) as List<Task>
        else listOf()
    }

    fun carryoverTasks() {
        val currentDate = LocalDate.now()
        val tasksToCarryOver = taskList.value!!.filter { task ->
            task.date.isBefore(currentDate) && !task.isFinished && task.carryover
        }
        tasksToCarryOver.forEach { task ->
            val date = when (task.repeat) {
                NONE -> return@forEach
                DAILY -> task.date.plusDays(1)
                WEEKLY -> task.date.plusWeeks(1)
                MONTHLY -> task.date.plusMonths(1)
            }
            if (date == LocalDate.now()) return@forEach
            val carryoverTask = task.copy(date = LocalDate.now())
            setRemindNotification(carryoverTask)
        }
    }

    fun repeatTasks() {
        taskList.value!!.filter { task ->
            task.date.isBefore(LocalDate.now()) && task.isFinished && task.repeat != NONE
        }.forEach { task ->
            val date = when (task.repeat) {
                NONE -> task.date
                DAILY -> task.date.plusDays(1)
                WEEKLY -> task.date.plusWeeks(1)
                MONTHLY -> task.date.plusMonths(1)
            }
            val subTaskList = task.subTasks.map { subtask ->
                subtask.copy(isFinished = false)
            }
            copyTask(task.copy(date = date, isFinished = false, subTasks = subTaskList.toMutableList()))
            deleteTask(task)
        }
    }

    private fun setRemindNotification(task: Task) {
        //期限が過去ならリマインドをしない
        if (task.date.isBefore(LocalDate.now())) return
        if (task.time == null) return
        if (task.time!!.isBefore(LocalTime.now())) return
        //タスクにリマインドが設定されていなければリマインドしない
        if (task.remind == ReminderInterval.NONE) return

        val context: Context = getApplication()
        val taskRemindIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = "com.cotrin.todolist.TASK_REMINDER_ACTION"
            putExtra("taskName", task.name)
            putExtra("taskRemind", task.remind.toString())
            putExtra("taskId", task.requestID)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(context, task.requestID, taskRemindIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.getRemindTimeInMills(), pendingIntent)
    }

    private fun cancelRemindNotification(task: Task) {
        val context: Context = getApplication()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = "com.cotrin.todolist.TASK_REMINDER_ACTION"
        }
        val pendingIntent = PendingIntent.getBroadcast(context, task.requestID, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun onTaskCardClick(view: View) {
        val el: ExpandableLayout = view.findViewById(R.id.expandableLayout)
        el.toggle()
        isExpansion.value = el.isExpanded
    }

    fun onTaskDetailClick(view: View, task: Task) {
        taskDetail.value = Pair(view, task)
        isDetailShown.value = true
    }

    fun onChecked(task: Task) {
        val isFinish = task.isFinished
        editTask(task.copy(isFinished = !isFinish))
        if (!isFinish) cancelRemindNotification(task)
        else setRemindNotification(task)
        task.subTasks.forEach {
            it.isFinished = !isFinish
        }
        editTask(task)
        taskList.value = taskList.value?.toList()
    }

    fun isBeforeDate(task: Task): Boolean {
        return task.date.isBefore(LocalDate.now())
    }
}