package com.github.walkandtag.firebase.db.schemas

import com.google.firebase.Timestamp
import com.mapbox.mapboxsdk.geometry.LatLng

data class PathSchema(
    var userId: String = "",
    val creationTimestamp: Timestamp = Timestamp.now(),
    var name: String = "",
    var description: String = "",
    var length: Double = 0.0,
    var time: Double = 0.0,
    var points: MutableList<LatLng> = mutableListOf()
)
