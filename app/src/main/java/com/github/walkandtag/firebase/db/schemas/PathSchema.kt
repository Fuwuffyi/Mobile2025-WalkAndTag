package com.github.walkandtag.firebase.db.schemas

data class Coordinate(
    var lat: Double = 0.0,
    var long: Double = 0.0
)

data class PathSchema(
    var userId: String,
    var name: String,
    var length: Float,
    var time: Float,
    var points: MutableList<Coordinate>
)