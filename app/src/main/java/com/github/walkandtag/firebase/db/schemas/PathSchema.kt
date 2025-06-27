package com.github.walkandtag.firebase.db.schemas

import org.maplibre.android.geometry.LatLng

data class PathSchema(
    var userId: String = "",
    var name: String = "",
    var length: Float = 0.0f,
    var time: Float = 0.0f,
    var points: MutableList<LatLng> = mutableListOf()
)