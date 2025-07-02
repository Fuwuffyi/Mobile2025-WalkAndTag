package com.github.walkandtag.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.walkandtag.R

class Notifier(
    private val channelId: String = "walkandtag_notification_channel",
    private val channelName: String = "WalkAndTag Notification Channel"
) {
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "App notifications"
        }
        if (context != null) {
            val manager =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(title: String, message: String, notificationId: Int = 0) {
        if (context == null) {
            Log.e("Notifier", "showNotification: Context not set for notification manager!")
        }
        val notification =
            NotificationCompat.Builder(context!!, channelId).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title).setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        NotificationManagerCompat.from(context!!).notify(notificationId, notification)
    }
}
