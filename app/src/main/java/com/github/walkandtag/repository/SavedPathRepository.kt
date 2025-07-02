package com.github.walkandtag.repository

import org.maplibre.android.geometry.LatLng

class SavedPathRepository() {
    private val _points = mutableListOf<LatLng>()
    val points: Collection<LatLng>
        get() = _points.toList()

    fun setPath(pathPoints: Collection<LatLng>) {
        _points.clear()
        _points.addAll(pathPoints)
    }

    fun clear() {
        _points.clear()
    }

    val isValid: Boolean
        get() = _points.size > 1
}