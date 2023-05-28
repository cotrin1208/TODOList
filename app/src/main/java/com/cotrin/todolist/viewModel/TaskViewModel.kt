package com.cotrin.todolist.viewModel

import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cotrin.todolist.R
import com.cotrin.todolist.Task
import com.cotrin.todolist.mainActivity.MainActivity
import com.cotrin.todolist.utils.GsonUtils
import com.cotrin.todolist.utils.Reference
import com.google.gson.reflect.TypeToken
import net.cachapa.expandablelayout.ExpandableLayout
import java.time.LocalDate
import java.util.UUID

class TaskViewModel: ViewModel() {
    val taskList = MutableLiveData<List<Task>>(listOf())
    val isDetailShown = MutableLiveData(false)
    val taskDetail = MutableLiveData<Pair<View, Task>>()
    val isExpansion = MutableLiveData(false)

    fun addTask(task: Task) {
        val updatedList = taskList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(task)
        taskList.value = updatedList
    }

    fun deleteTask(task: Task) {
        val updatedList = taskList.value?.toMutableList() ?: return
        updatedList.remove(task)
        taskList.value = updatedList
    }

    fun editTask(task: Task) {
        val updatedList = taskList.value?.toMutableList() ?: return
        val index = updatedList.indexOfFirst { it.uuid == task.uuid }
        if (index != -1) {
            updatedList[index] = task.copy(uuid = UUID.randomUUID())
            taskList.value = updatedList
        }
        saveTasks()
    }

    fun copyTask(task: Task) {
        val copyTask = task.copy(uuid = UUID.randomUUID())
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
        taskList.value!!.filter {
            it.date.isBefore(LocalDate.now())
        }.filter {
            !it.isFinished
        }.filter {
            it.carryover
        }.forEach {
            it.date = it.date.plusDays(1)
        }
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
        editTask(task.copy(isFinished = !task.isFinished))
        taskList.value = taskList.value?.toList()
    }
}