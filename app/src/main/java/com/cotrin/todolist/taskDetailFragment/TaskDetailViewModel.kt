package com.cotrin.todolist.taskDetailFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cotrin.todolist.model.ReminderInterval
import com.cotrin.todolist.model.RepeatInterval
import com.cotrin.todolist.model.TaskCategory
import com.cotrin.todolist.utils.Reference
import java.time.LocalDate
import java.time.LocalTime

class TaskDetailViewModel: ViewModel() {
    val name = MutableLiveData("")
    val date = MutableLiveData(LocalDate.now())
    val time = MutableLiveData(LocalTime.now())
    val category = MutableLiveData(TaskCategory.OTHER)
    val remind = MutableLiveData(ReminderInterval.NONE)
    val repeat = MutableLiveData(RepeatInterval.NONE)
    val carryover = MutableLiveData(false)

    fun getTimeText(): String {
        return time.value?.format(Reference.TIME_FORMATTER) ?: run { "**:**" }
    }
}