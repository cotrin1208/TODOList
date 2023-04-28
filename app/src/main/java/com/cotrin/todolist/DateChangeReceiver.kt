package com.cotrin.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate

class DateChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        //アクション文字列が一致しない場合早期リターン
        if (intent?.action != "android.intent.action.BOOT_COMPLETED") return
        //前日のタスク一覧を取得
        if (Task.taskList.containsKey(LocalDate.now().minusDays(1))) return
        val previousTasks = Task.taskList[LocalDate.now().minusDays(1)]!!
        //前日のタスクから翌日繰り越し、リピート設定のタスクを検索する
        previousTasks.forEach {
            //リピート処理
            if (it.repeatInterval != RepeatInterval.NONE)
                Task.addTaskByRepeatInterval(it)

            //翌日繰り越し処理
            if (it.carryOver && !it.isFinished)
                Task.carryoverNextDay(it)
        }
    }
}