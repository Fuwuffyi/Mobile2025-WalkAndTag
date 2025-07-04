package com.github.walkandtag.firebase.db

data class FirestoreDocument<T>(
    val id: String,
    val data: T
)

data class PagedResult<T>(
    val documents: Collection<FirestoreDocument<T>>,
    val lastDocumentId: String? = null
)
