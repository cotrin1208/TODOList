package com.cotrin.todolist.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cotrin.todolist.Task
import com.cotrin.todolist.utils.Reference
import java.util.UUID

class MainActivityViewModel: ViewModel() {
    val taskList = MutableLiveData<List<Task>>()
    val isAddFragmentShown = MutableLiveData(false)
    val isEditFragmentShown = MutableLiveData(false)
    val taskData = MutableLiveData(Task())
    var task: Task
        get() { return taskData.value!! }
        set(value) { taskData.value = value }

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
            updatedList[index] = task
            taskList.value = updatedList
        }
    }

    fun copyTask(task: Task) {
        val copyTask = task.copy(uuid = UUID.randomUUID())
        addTask(copyTask)
    }

    fun onAddTaskButtonClick() {
        isAddFragmentShown.value = true
    }

    fun onTaskClick() {
        isEditFragmentShown.value = true
    }
}