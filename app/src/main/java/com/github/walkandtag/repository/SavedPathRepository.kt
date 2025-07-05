package com.github.walkandtag.repository

import com.mapbox.mapboxsdk.geometry.LatLng

class SavedPathRepository {
    private val _points = mutableListOf<LatLng>()
    val points: Collection<LatLng>
        get() = _points.toList()

    fun addPoint(point: LatLng) {
        _points.add(point)
    }

    fun clear() {
        _points.clear()
    }

    val isValid: Boolean
        get() = _points.size > 1
}
