package com.github.walkandtag.firebase.db.schemas

// @TODO(): Think about implementing profile picture
data class UserSchema(
    var username: String = "",
    var favoritePathIds: MutableList<String> = mutableListOf()
)
