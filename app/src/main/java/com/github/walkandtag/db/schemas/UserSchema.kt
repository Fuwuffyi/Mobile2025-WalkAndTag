package com.github.walkandtag.db.schemas

data class UserSchema(
    override var id: String? = null,
    var username: String = "",
    var email: String = ""
) : Schema