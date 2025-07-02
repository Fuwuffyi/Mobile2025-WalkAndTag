package com.github.walkandtag.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.walkandtag.R

class Notifier(
    private val appContext: Context,
    private val channelId: String = "walkandtag_notifications",
    private val channelName: String = "WalkAndTag Notifications",
    private val channelDescription: String = "General app notifications",
    private val channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    private val smallIconRes: Int = R.drawable.notification_icon
) {
    init {
        require(appContext.applicationContext === appContext) {
            "Please pass Application context to avoid leaking Activities"
        }
        createNotificationChannelIfNeeded()
    }

    private var channelCreated: Boolean = false

    private fun createNotificationChannelIfNeeded() {
        if (!channelCreated) {
            val channel = NotificationChannel(channelId, channelName, channelImportance).apply {
                description = channelDescription
            }
            val mgr =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(channel)
            channelCreated = true
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notify(
        title: String,
        text: String,
        notificationId: Int,
        ongoing: Boolean = false,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                appContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("POST_NOTIFICATIONS permission not granted")
        }
        createNotificationChannelIfNeeded()
        val builder = NotificationCompat.Builder(appContext, channelId).setSmallIcon(smallIconRes)
            .setContentTitle(title).setContentText(text).setPriority(priority).setOngoing(ongoing)
        val notification = builder.build()
        NotificationManagerCompat.from(appContext).notify(notificationId, notification)
        return builder.build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyPersistent(
        title: String,
        text: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                appContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("POST_NOTIFICATIONS permission not granted")
        }
        createNotificationChannelIfNeeded()
        val builder = NotificationCompat.Builder(appContext, channelId).setSmallIcon(smallIconRes)
            .setContentTitle(title).setContentText(text).setPriority(priority).setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        return builder.build()
    }

    fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(appContext).cancel(notificationId)
    }
}
