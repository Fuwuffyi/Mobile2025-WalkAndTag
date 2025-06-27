package com.github.walkandtag.firebase.db

data class FirestoreDocument<T>(
    val id: String,
    val data: T
)