package com.cotrin.todolist.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cotrin.todolist.R
import com.cotrin.todolist.mainActivity.MainActivity
import com.cotrin.todolist.model.ReminderInterval.valueOf
import com.cotrin.todolist.utils.Reference

class TaskReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("taskName")
        val reminderInterval = valueOf(intent.getStringExtra("taskRemind")!!)
        val taskId = intent.getIntExtra("taskId", 0)

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, Reference.APP_ID).apply {
            setSmallIcon(R.drawable.notification_icon)
            setContentTitle("リマインダー：$taskName")
            setContentText(reminderInterval.notifyMessage)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId, builder.build())
    }
}