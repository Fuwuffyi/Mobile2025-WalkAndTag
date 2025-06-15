package com.github.walkandtag.firebase.db.schemas

data class UserSchema(
    override var id: String? = null,
    var username: String = "",
) : Schema