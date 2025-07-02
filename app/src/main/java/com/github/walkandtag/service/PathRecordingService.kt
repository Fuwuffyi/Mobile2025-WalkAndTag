package com.github.walkandtag.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.github.walkandtag.util.Notifier
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import org.maplibre.android.geometry.LatLng

class PathRecordingService : Service() {
    private val pathRecordingNotificationId = 420
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val pathPoints = mutableListOf<LatLng>()
    private val notifier: Notifier by inject()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        super.onCreate()
        val notification = notifier.notifyPersistent(
            title = "Recording Path",
            text = "Your walk is being recorded.",
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

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        notifier.cancel(pathRecordingNotificationId)
        coroutineScope.cancel()
        // @TODO(): Persist each point for crash safety
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(10f).setMinUpdateIntervalMillis(1000L).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    addPoint(location)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun addPoint(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        pathPoints.add(latLng)
        // @TODO(): Persist each point for crash safety
    }

    fun getPath(): Collection<LatLng> = pathPoints
}