package com.cotrin.todolist.mainActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cotrin.todolist.enums.ReminderInterval
import com.cotrin.todolist.enums.RepeatInterval
import com.cotrin.todolist.enums.TaskCategory
import com.cotrin.todolist.realm.Task
import java.time.LocalDate
import java.time.LocalTime

class MainActivityViewModel: ViewModel() {
    val isAddFragmentShown = MutableLiveData(false)
    val taskData = MutableLiveData(Task())

    fun setName(value: String) {
        taskData.value = taskData.value?.copy(name = value)
    }

    fun setDate(value: LocalDate) {
        taskData.value = taskData.value?.copy(date = value)
    }

    fun setTime(value: LocalTime?) {
        taskData.value = taskData.value?.copy(time = value)
    }

    fun setRemind(value: ReminderInterval) {
        taskData.value = taskData.value?.copy(remind = value)
    }

    fun setRepeat(value: RepeatInterval) {
        taskData.value = taskData.value?.copy(repeat = value)
    }

    fun setCarryover(value: Boolean) {
        taskData.value = taskData.value?.copy(carryover = value)
    }

    fun setCategory(value: TaskCategory) {
        taskData.value = taskData.value?.copy(category = value)
    }

    fun isNullTime(): Boolean {
        return taskData.value!!.time?.let { false } ?: true
    }

    fun clearTask() {
        taskData.value = Task()
    }

    fun onAddTaskButtonClick() {
        isAddFragmentShown.value = true
    }
}