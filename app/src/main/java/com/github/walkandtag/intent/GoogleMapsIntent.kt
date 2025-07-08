package com.github.walkandtag.intent

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.github.walkandtag.R
import com.mapbox.mapboxsdk.geometry.LatLng

object GoogleMapsIntent {

    fun openGoogleMapsNavigation(
        context: Context,
        start: LatLng,
        end: LatLng
    ) {
        try {
            val uri =
                "https://www.google.com/maps/dir/?api=1&origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&travelmode=walking".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                openWebMapsNavigation(context, start, end)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.error_opening_maps),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openWebMapsNavigation(
        context: Context,
        start: LatLng,
        end: LatLng
    ) {
        val uri = "https://www.google.com/maps/dir/${start.latitude},${start.longitude}/${end.latitude},${end.longitude}".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_maps_app_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}