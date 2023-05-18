package com.cotrin.todolist

import com.cotrin.todolist.ReminderInterval.NONE
import com.cotrin.todolist.RepeatInterval.*
import com.cotrin.todolist.mainActivity.MainActivity
import com.cotrin.todolist.utils.GsonUtils
import com.cotrin.todolist.utils.Reference
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class Task(
    val name: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime? = null,
    val remindInterval: ReminderInterval = NONE,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val carryover: Boolean = false,
    val category: TaskCategory = TaskCategory.OTHER,
    val subTasks: MutableList<SubTask> = mutableListOf(),
    val isFinished: Boolean = false,
    val uuid: UUID = UUID.randomUUID(),
    val requestID: Int = generateRequestID()
) {
    companion object {
        var taskList = mutableListOf<Task>()
        val generateRequestID = getRequestID()

        //シーケンシャルID生成
        private fun getRequestID(): () -> Int {
            var id = MainActivity.sharedPreferences.getInt(Reference.REQUEST_ID_START, 0)
            return {
                id++
                id
            }
        }

        //タスク追加
        fun addTask(task: Task) {
            taskList.add(task)
            val editor = MainActivity.sharedPreferences.edit()
            editor.putInt(Reference.REQUEST_ID_START, task.requestID)
            editor.apply()
            saveTasks()
        }

        //タスク削除
        fun removeTaskByUUID(uuid: UUID) {
            taskList.removeIf { it.uuid == uuid }
        }

        //タスク編集
        fun editTask(index: Int, task: Task) {
            taskList[index] = task
        }

        //RepeatIntervalの値に応じてタスクを追加
        fun addTaskByRepeatInterval(task: Task) {
            val date = when (task.repeatInterval) {
                RepeatInterval.NONE -> null
                DAILY -> task.date.plusDays(1)
                WEEKLY -> task.date.plusWeeks(1)
                MONTHLY -> task.date.plusMonths(1)
            }
            date?.let {
                addTask(task.copy(date = it))
            }
        }

        //タスク保存
        fun saveTasks() {
            val editor = MainActivity.sharedPreferences.edit()
            val gson = GsonUtils.getCustomGson()
            val json = gson.toJson(taskList)
            editor.putString(Reference.TASK_LIST, json)
            editor.apply()
        }

        //タスク読み込み
        fun loadTasks() {
            val gson = GsonUtils.getCustomGson()
            val json = MainActivity.sharedPreferences.getString(Reference.TASK_LIST, null)
            val type = object: TypeToken<MutableMap<LocalDate, MutableList<Task>>>(){}.type
            taskList = if (json != null) gson.fromJson(json, type) as MutableList<Task>
            else mutableListOf()
        }

        fun carryoverPreviousTasks(date: LocalDate) {
            taskList.filter {
                it.date.isBefore(date)
            }.filter {
                !it.isFinished
            }.filter {
                it.carryover
            }.forEach {
                it.date.plusDays(1)
            }
        }
    }

    fun toJsonString(): String {
        val gson = GsonUtils.getCustomGson()
        return gson.toJson(this)
    }
}
