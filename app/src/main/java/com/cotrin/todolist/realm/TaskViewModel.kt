package com.cotrin.todolist.realm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.cotrin.todolist.R
import com.cotrin.todolist.enums.ReminderInterval
import com.cotrin.todolist.enums.RepeatInterval.DAILY
import com.cotrin.todolist.enums.RepeatInterval.MONTHLY
import com.cotrin.todolist.enums.RepeatInterval.NONE
import com.cotrin.todolist.enums.RepeatInterval.WEEKLY
import com.cotrin.todolist.notification.TaskReminderReceiver
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmUUID
import net.cachapa.expandablelayout.ExpandableLayout
import java.time.LocalDate
import java.time.LocalTime

class TaskViewModel(application: Application): AndroidViewModel(application) {
    val taskList = MutableLiveData<List<Task>>(listOf())
    val isDetailShown = MutableLiveData(false)
    val taskDetail = MutableLiveData<Pair<View, Task>>()
    val isExpansion = MutableLiveData(false)
    val realm = RealmUtil.getRealm()

    fun addTask(task: Task) {
        realm.writeBlocking {
            copyToRealm(task)
            setRemindNotification(task)
        }
    }

    fun deleteTask(task: Task) {
        realm.writeBlocking {
            val deleteTask = this.query<Task>("uuid == $0", task.uuid).find().first()
            delete(deleteTask)
            cancelRemindNotification(task)
        }
    }

    fun editTask(task: Task) {
        realm.writeBlocking {
            copyToRealm(task, UpdatePolicy.ALL)
            cancelRemindNotification(task)
            setRemindNotification(task)
        }
    }

    fun copyTask(task: Task) {
        realm.writeBlocking {
            copyToRealm(task.copy(uuid = RealmUUID.random(), requestID = IDGenerator.generateID()))
            setRemindNotification(task)
        }
    }

    fun addSubTask(task: Task) {
        realm.writeBlocking {
            val targetTask = this.query<Task>("uuid == $0", task.uuid).find().first()
            targetTask.subTasks.add(SubTask())
        }
    }

    fun deleteSubTask(task: Task, position: Int) {
        realm.writeBlocking {
            val targetTask = this.query<Task>("uuid == $0", task.uuid).find().first()
            targetTask.subTasks.removeAt(position)
        }
    }

    fun editSubTaskName(task: Task, position: Int, name: String) {
        realm.writeBlocking {
            val targetTask = findLatest(task.subTasks[position]) ?: return@writeBlocking
            targetTask.name = name
        }
    }

    fun toggleSubTask(task: Task, position: Int) {
        realm.writeBlocking {
            val targetTask = this.query<Task>("uuid == $0", task.uuid).find().first()
            val isChecked = targetTask.subTasks[position].isFinished
            targetTask.subTasks[position] = targetTask.subTasks[position].copy(isFinished = !isChecked)
            for (subTask in targetTask.subTasks) {
                if (!subTask.isFinished) {
                    targetTask.isFinished = false
                    setRemindNotification(targetTask)
                    return@writeBlocking
                }
            }
            targetTask.isFinished = true
            cancelRemindNotification(targetTask)
            copyToRealm(targetTask, UpdatePolicy.ALL)
        }
    }

    fun carryoverTasks() {
        val realm = RealmUtil.getRealm()
        realm.writeBlocking {
            val tasks = this.query<Task>().find()
            val currentDate = LocalDate.now()
            tasks.filter {
                it.date.isBefore(currentDate) && !it.isFinished && it.carryover
            }.forEach {
                val date = when (it.repeat) {
                    NONE -> it.date
                    DAILY -> it.date.plusDays(1)
                    WEEKLY -> it.date.plusWeeks(1)
                    MONTHLY -> it.date.plusMonths(1)
                }
                if (date == currentDate) return@writeBlocking
                it.date = currentDate
            }
        }
    }

    fun repeatTasks() {
        val realm = RealmUtil.getRealm()
        realm.writeBlocking {
            val tasks = this.query<Task>().find()
            val currentDate = LocalDate.now()
            tasks.filter {
                it.date.isBefore(currentDate) && it.isFinished && it.repeat != NONE
            }.forEach {
                val date = when (it.repeat) {
                    NONE -> it.date
                    DAILY -> it.date.plusDays(1)
                    WEEKLY -> it.date.plusWeeks(1)
                    MONTHLY -> it.date.plusMonths(1)
                }
                val subTasks = it.subTasks.map {  subTask ->
                    subTask.copy(isFinished = false)
                }.toRealmList()
                val copiedTask = it.copy(date = date, isFinished = false, subTasks = subTasks, requestID = IDGenerator.generateID())
                delete(it)
                copyToRealm(copiedTask, UpdatePolicy.ALL)
            }
        }
    }

    fun setAllReminders() {
        val realm = RealmUtil.getRealm()
        realm.writeBlocking {
            val tasks = this.query<Task>().find()
            tasks.forEach {
                cancelRemindNotification(it)
                setRemindNotification(it)
            }
        }
    }

    private fun toggleCheck(task: Task) {
        editTask(task.copy(isFinished = !task.isFinished))
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
        toggleCheck(task)
        if (task.isFinished) {
            //通知を設定する
            setRemindNotification(task)
        } else {
            //通知を解除する
            cancelRemindNotification(task)
        }
        //サブタスクの完了状況を同期
        realm.writeBlocking {
            val targetTask = query<Task>("uuid == $0", task.uuid).find().first()
            targetTask.subTasks.replaceAll {
                it.copy(isFinished = !task.isFinished)
            }
        }
    }
}