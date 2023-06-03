package com.cotrin.todolist.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cotrin.todolist.model.Task

class MainActivityViewModel: ViewModel() {
    val taskList = MutableLiveData<List<Task>>(listOf())
    val isAddFragmentShown = MutableLiveData(false)
    val taskData = MutableLiveData(Task())
    var task: Task
        get() { return taskData.value!! }
        set(value) { taskData.value = value }

    fun onAddTaskButtonClick() {
        isAddFragmentShown.value = true
    }
}