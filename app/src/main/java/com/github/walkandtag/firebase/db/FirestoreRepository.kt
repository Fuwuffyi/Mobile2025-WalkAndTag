package com.github.walkandtag.firebase.db

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository<T : Any>(
    private val col: CollectionReference, private val classType: Class<T>
) {
    suspend fun create(item: T): String = col.add(item).await().id

    suspend fun create(item: T, id: String): String {
        col.document(id).set(item).await()
        return id
    }

    suspend fun get(id: String): T? = col.document(id).get().await().toObject(classType)

    suspend fun get(ids: Collection<String>): Collection<T> {
        if (ids.isEmpty()) return emptyList()
        return ids.chunked(10).flatMap { chunk ->
            col.whereIn(FieldPath.documentId(), chunk).get().await().toObjects(classType)
        }
    }

    suspend fun getAll(): Collection<T> = col.get().await().toObjects(classType)

    suspend fun update(item: T, id: String) {
        col.document(id).set(item).await()
    }

    suspend fun delete(id: String) = col.document(id).delete().await()

    companion object {
        inline fun <reified T : Any> create(
            collectionPath: String, firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        ): FirestoreRepository<T> {
            return FirestoreRepository(
                firestore.collection(collectionPath), T::class.java
            )
        }
    }
}