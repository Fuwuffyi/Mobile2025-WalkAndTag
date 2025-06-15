package com.github.walkandtag.firebase.db

import com.github.walkandtag.firebase.db.schemas.Schema
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository<T : Schema>(
    private val col: CollectionReference,
    private val classType: Class<T>
) {
    suspend fun create(item: T): String =
        if (item.id == null) {
            col.add(item).await().id.also { item.id = it }
        } else {
            col.document(item.id!!).set(item).await()
            item.id!!
        }

    suspend fun get(id: String): T? =
        col.document(id).get().await().toObject(classType)

    suspend fun getAll(): List<T> =
        col.get().await().toObjects(classType)

    suspend fun update(item: T) {
        val id = item.id ?: throw IllegalArgumentException("ID required")
        col.document(id).set(item).await()
    }

    suspend fun delete(id: String) =
        col.document(id).delete().await()

    companion object {
        inline fun <reified T : Schema> create(
            collectionPath: String,
            firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        ): FirestoreRepository<T> {
            return FirestoreRepository(
                firestore.collection(collectionPath),
                T::class.java
            )
        }
    }
}