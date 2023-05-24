package com.cotrin.todolist.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.cotrin.todolist.Task

class TaskViewModel: ViewModel() {
    private val _taskList = MutableLiveData(listOf<Task>())
    val taskList = _taskList.distinctUntilChanged()


}