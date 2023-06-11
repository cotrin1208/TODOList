package com.cotrin.todolist.realm

import android.view.View
import com.cotrin.todolist.enums.ReminderInterval
import com.cotrin.todolist.enums.RepeatInterval
import com.cotrin.todolist.enums.TaskCategory
import com.cotrin.todolist.utils.Reference
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class Task(): RealmObject {
    //タスク名
    var name = ""
    //日付
    private var _date: Long = LocalDate.now().toEpochDay()
    var date: LocalDate
        get() { return LocalDate.ofEpochDay(_date) }
        set(value) { _date = value.toEpochDay() }
    //時刻
    private var _time: String? = null
    var time: LocalTime?
        get() { return _time?.let { LocalTime.parse(_time) } }
        set(value) { value?.let { _time = it.toString() } ?: { _time = null } }
    //リマインド
    private var _remind: String = ReminderInterval.NONE.toString()
    var remind: ReminderInterval
        get() { return ReminderInterval.valueOf(_remind) }
        set(value) { _remind = value.toString() }
    //リピート
    private var _repeat: String = RepeatInterval.NONE.toString()
    var repeat: RepeatInterval
        get() { return RepeatInterval.valueOf(_repeat) }
        set(value) { _repeat = value.toString() }
    //繰り越し
    var carryover = false
    //カテゴリ
    private var _category: String = TaskCategory.OTHER.toString()
    var category: TaskCategory
        get() { return TaskCategory.valueOf(_category) }
        set(value) { _category = value.toString() }
    //サブタスク
    var subTasks: RealmList<SubTask> = realmListOf()
    //完了フラグ
    var isFinished = false
    //固有識別子
    @PrimaryKey
    var uuid = RealmUUID.random()
    //NotificationリクエストID
    var requestID = IDGenerator.generateID()

    constructor(
        name: String = "",
        date: LocalDate = LocalDate.now(),
        time: LocalTime? = null,
        remind: ReminderInterval = ReminderInterval.NONE,
        repeat: RepeatInterval = RepeatInterval.NONE,
        carryover: Boolean = false,
        category: TaskCategory = TaskCategory.OTHER,
        subTasks: RealmList<SubTask> = realmListOf(),
        isFinished: Boolean = false,
        uuid: RealmUUID = RealmUUID.random(),
        requestID: Int = IDGenerator.generateID()
        ) : this() {
        this.name = name
        this.date = date
        this.time = time
        this.remind = remind
        this.repeat = repeat
        this.carryover = carryover
        this.category = category
        this.subTasks = subTasks
        this.isFinished = isFinished
        this.uuid = uuid
        this.requestID = requestID
    }

    fun getRemindTimeInMills(): Long {
        val remindDateTime = LocalDateTime.of(date, time)
        val reminderTime = remindDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intervalMills = remind.minute * 60 * 1000
        return reminderTime - intervalMills
    }

    fun copy(name: String = this.name,
             date: LocalDate = this.date,
             time: LocalTime? = this.time,
             remind: ReminderInterval = this.remind,
             repeat: RepeatInterval = this.repeat,
             carryover: Boolean = this.carryover,
             category: TaskCategory = this.category,
             subTasks: RealmList<SubTask> = this.subTasks,
             isFinished: Boolean = this.isFinished,
             uuid: RealmUUID = this.uuid,
             requestID: Int = this.requestID): Task {
        return Task(name, date, time, remind, repeat, carryover, category, subTasks, isFinished, uuid, requestID)
    }

    fun getTimeText(): String {
        return time?.format(Reference.TIME_FORMATTER) ?: run { "**:**" }
    }

    fun getDateText(): String {
        return date.format(Reference.MONTH_DAY_FORMATTER)
    }

    fun getVisibilityTimeText(): Int {
        return time?.let { View.VISIBLE } ?: View.GONE
    }

    fun getVisibilityRemindIcon(): Int {
        return if (remind != ReminderInterval.NONE) View.VISIBLE else View.GONE
    }

    fun getVisibilityRepeatIcon(): Int {
        return if (repeat != RepeatInterval.NONE) View.VISIBLE else View.GONE
    }

    fun getVisibilityCarryoverIcon(): Int {
        return if (carryover) View.VISIBLE else View.GONE
    }
}