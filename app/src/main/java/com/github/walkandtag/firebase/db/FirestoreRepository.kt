package com.github.walkandtag.firebase.db

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private data class CachedData<T>(
    val data: T, val timestamp: Long = System.currentTimeMillis()
)

class FirestoreRepository<T : Any>(
    private val docRef: CollectionReference,
    private val classType: Class<T>,
    private val expiryMillis: Long = TimeUnit.MINUTES.toMillis(5)
) {
    private val cache = ConcurrentHashMap<String, CachedData<T>>()

    suspend fun create(item: T, id: String): String {
        docRef.document(id).set(item).await()
        return id
    }

    suspend fun get(id: String): FirestoreDocument<T>? {
        cache[id]?.let {
            if (System.currentTimeMillis() - it.timestamp < expiryMillis) {
                return FirestoreDocument(id, it.data)
            } else {
                cache.remove(id)
            }
        }
        val cachedDoc = runCatching {
            this.docRef.document(id).get(Source.CACHE).await()
        }.getOrNull()
        val docSnapshot = if (cachedDoc != null && cachedDoc.exists()) {
            cachedDoc
        } else {
            val networkSnapshot = docRef.document(id).get(Source.SERVER).await()
            if (!networkSnapshot.exists()) {
                throw NoSuchElementException("Document $id not found")
            }
            networkSnapshot
        }
        val data = docSnapshot.toObject(classType)
            ?: throw NoSuchElementException("Failed to parse document $id")
        cache[id] = CachedData(data)
        return FirestoreDocument(id, data)
    }

    suspend fun get(ids: Collection<String>): Collection<FirestoreDocument<T>> {
        if (ids.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()
        val (freshCache, staleOrMissing) = ids.partition { id ->
            cache[id]?.let { now - it.timestamp < expiryMillis } == true
        }
        val cachedDocs = freshCache.mapNotNull { id ->
            cache[id]?.let { FirestoreDocument(id, it.data) }
        }
        val fetchedDocs = staleOrMissing.chunked(10).flatMap { chunk ->
            docRef.whereIn(FieldPath.documentId(), chunk).get().await().documents.mapNotNull { doc ->
                val obj = doc.toObject(classType)
                obj?.let {
                    cache[doc.id] = CachedData(it) // Cache the fresh doc
                    FirestoreDocument(doc.id, it)
                }
            }
        }
        return cachedDocs + fetchedDocs
    }

    suspend fun getAll(): List<FirestoreDocument<T>> {
        val snapshot = docRef.get().await()
        val now = System.currentTimeMillis()
        return snapshot.documents.mapNotNull { doc ->
            val obj = doc.toObject(classType)
            obj?.let {
                cache[doc.id] = CachedData(it, now)
                FirestoreDocument(doc.id, it)
            }
        }
    }

    suspend fun update(item: T, id: String) {
        docRef.document(id).set(item).await()
        cache[id] = CachedData(item)
    }

    suspend fun delete(id: String) {
        docRef.document(id).delete().await()
        cache.remove(id)
    }

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