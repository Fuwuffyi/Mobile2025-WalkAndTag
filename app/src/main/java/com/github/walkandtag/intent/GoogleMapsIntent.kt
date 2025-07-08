package com.github.walkandtag.intent

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.github.walkandtag.R
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import org.koin.java.KoinJavaComponent.inject

fun openGoogleMapsNavigation(
    context: Context,
    paddedPoints: List<LatLng>,
) {
    val globalViewModel: GlobalViewModel by inject(
        clazz = GlobalViewModel::class.java
    )
    try {
        val start = paddedPoints.first()
        val end = paddedPoints.last()
        val waypoints = paddedPoints
            .subList(1, paddedPoints.size - 1)
            .joinToString("|") { "${it.latitude},${it.longitude}" }

        val uriBuilder = StringBuilder()
            .append("https://www.google.com/maps/dir/?api=1")
            .append("&origin=${start.latitude},${start.longitude}")
            .append("&destination=${end.latitude},${end.longitude}")
            .append("&travelmode=walking")

        if (waypoints.isNotEmpty()) {
            uriBuilder.append("&waypoints=$waypoints")
        }

        val uri = uriBuilder.toString().toUri()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            openWebMapsNavigation(context, paddedPoints)
        }

    } catch (e: Exception) {
        globalViewModel.showSnackbar(context.getString(R.string.error_opening_maps))
        Log.e("MAPS_INTENT", "Could not open Google Maps.", e)
    }
}

private fun openWebMapsNavigation(
    context: Context,
    paddedPoints: List<LatLng>
) {
    val pathPart = paddedPoints.joinToString("/") { "${it.latitude},${it.longitude}" }
    val uri = "https://www.google.com/maps/dir/$pathPart/data=!4m2!4m1!3e2".toUri()

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}

