package com.github.walkandtag.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.github.walkandtag.R
import com.github.walkandtag.repository.SavedPathRepository
import com.github.walkandtag.util.Notifier
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

class PathRecordingService : Service() {
    private val pathRecordingNotificationId = 420
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val notifier: Notifier by inject()
    private val savedPathRepo: SavedPathRepository by inject()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        super.onCreate()
        val notification = notifier.notifyPersistent(
            title = resources.getString(R.string.recording_path),
            text = resources.getString(R.string.walk_recording),
            priority = NotificationCompat.PRIORITY_LOW
        )
        startForeground(pathRecordingNotificationId, notification)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        notifier.cancel(pathRecordingNotificationId)
        coroutineScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(5f).setMinUpdateIntervalMillis(1000L).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    addPoint(location)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        if (!savedPathRepo.isValid) {
            notifier.notify(
                title = resources.getString(R.string.path_not_saved),
                text = resources.getString(R.string.error_saving_path),
                notificationId = pathRecordingNotificationId + 1
            )
            savedPathRepo.clear()
            return;
        }
    }

    private fun addPoint(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        savedPathRepo.addPoint(latLng)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onTaskRemoved(rootIntent: Intent?) {
        stopLocationUpdates()
        stopSelf()
    }
}
