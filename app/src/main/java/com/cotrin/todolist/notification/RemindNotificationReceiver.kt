package com.cotrin.todolist.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.cotrin.todolist.R
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.utils.getTaskExtra

class RemindNotificationReceiver: BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "remind_notification_channel"
        private const val CHANNEL_NAME = "リマインド通知"
        private const val CHANNEL_DESCRIPTION = "リマインド通知"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val toast = Toast.makeText(context, "アラームによる処理が実行されました。", Toast.LENGTH_SHORT)
        toast.show()

        val task = intent.getTaskExtra(Reference.TASK)
        //通知を作成して表示
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createRemindNotificationChannel(notificationManager)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle("リマインダー")
            setContentText("タスク名「${task.name}」")
            setSmallIcon(R.drawable.task_attribute_remind)
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(true)
        }.build()

        notificationManager.notify(task.requestID, notification)
    }

    private fun createRemindNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                notificationManager.createNotificationChannel(this@apply)
            }
        }
    }
}