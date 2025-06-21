package com.github.walkandtag.firebase.db.schemas

import org.maplibre.android.geometry.LatLng

data class PathSchema(
    var userId: String,
    var name: String,
    var length: Float,
    var time: Float,
    var points: MutableList<LatLng>
)