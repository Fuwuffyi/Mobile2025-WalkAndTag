package com.github.walkandtag.firebase.db.schemas

data class UserSchema(
    var username: String = "",
    var favoritePathIds: MutableList<String> = mutableListOf()
)
