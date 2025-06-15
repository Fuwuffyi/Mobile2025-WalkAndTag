package com.github.walkandtag.db

import com.github.walkandtag.db.schemas.Schema
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FirestoreDAO<T : Schema>(
    private val collectionPath: String,
    private val classType: Class<T>
) {
    private val colRef: CollectionReference get() = Firebase.firestore.collection(collectionPath)

    fun create(data: T): Task<String> {
        return if (data.id == null) {
            colRef.add(data)
                .continueWith { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    task.result!!.id
                }
        } else {
            colRef.document(data.id!!).set(data)
                .continueWith { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    data.id!!
                }
        }
    }

    fun get(id: String): Task<T?> =
        colRef.document(id)
            .get()
            .continueWith { task ->
                if (!task.isSuccessful) throw task.exception!!
                task.result?.toObject(classType)
            }

    fun getAll(): Task<List<T>> =
        colRef.get()
            .continueWith { task ->
                if (!task.isSuccessful) throw task.exception!!
                task.result!!.toObjects(classType)
            }

    fun update(data: T): Task<Void> {
        val id = data.id
            ?: return Tasks.forException(IllegalArgumentException("ID required for update"))
        return colRef.document(id).set(data)
    }

    fun delete(id: String): Task<Void> =
        colRef.document(id).delete()
}