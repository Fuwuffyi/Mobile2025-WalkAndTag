package com.github.walkandtag.repository

import android.util.Log
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.PagedResult
import com.github.walkandtag.firebase.db.schemas.FirestoreQueryBuilder
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
    private val expiryMillis: Long = TimeUnit.MINUTES.toMillis(60)
) {
    private val cache = ConcurrentHashMap<String, CachedData<T>>()

    suspend fun create(item: T, id: String? = null): String {
        val ref = id?.let { docRef.document(it) } ?: docRef.document()
        ref.set(item).await()
        return ref.id
    }

    suspend fun get(id: String): FirestoreDocument<T>? {
        cache[id]?.let {
            if (System.currentTimeMillis() - it.timestamp < expiryMillis) {
                return FirestoreDocument(id, it.data)
            } else cache.remove(id)
        }
        val docSnapshot = runCatching {
            docRef.document(id).get(Source.CACHE).await()
        }.getOrNull()?.takeIf { it.exists() } ?: docRef.document(id).get(Source.SERVER).await()
            .takeIf { it.exists() } ?: run {
            Log.w("FirestoreRepository", "Document $id not found")
            return null
        }
        val obj = docSnapshot.toObject(classType) ?: return null
        cache[id] = CachedData(obj)
        return FirestoreDocument(docSnapshot.id, obj)
    }

    suspend fun get(ids: Collection<String>): Collection<FirestoreDocument<T>> {
        if (ids.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()
        val (cached, fetch) = ids.partition { id ->
            cache[id]?.let { now - it.timestamp < expiryMillis } == true
        }
        val cachedDocs = cached.mapNotNull { id ->
            cache[id]?.let { FirestoreDocument(id, it.data) }
        }
        val fetchedDocs = fetch.chunked(10).flatMap { chunk ->
            docRef.whereIn(FieldPath.documentId(), chunk).get()
                .await().documents.mapNotNull { doc ->
                    doc.toObject(classType)?.let {
                        cache[doc.id] = CachedData(it, now)
                        FirestoreDocument(doc.id, it)
                    }
                }
        }
        return cachedDocs + fetchedDocs
    }

    suspend fun getAll(limit: UInt = 1000u): Collection<FirestoreDocument<T>> {
        val snapshot = docRef.limit(limit.toLong()).get().await()
        val now = System.currentTimeMillis()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(classType)?.let {
                cache[doc.id] = CachedData(it, now)
                FirestoreDocument(doc.id, it)
            }
        }
    }

    suspend fun getAllPaged(limit: UInt = 15u, startAfterId: String? = null): PagedResult<T> {
        var query = docRef.limit(limit.toLong())
        if (startAfterId != null) {
            val doc = docRef.document(startAfterId).get().await()
            if (doc.exists()) query = query.startAfter(doc)
        }
        val snapshot = query.get().await()
        val now = System.currentTimeMillis()
        val docs = snapshot.documents.mapNotNull { doc ->
            doc.toObject(classType)?.let {
                cache[doc.id] = CachedData(it, now)
                FirestoreDocument(doc.id, it)
            }
        }
        return PagedResult(docs, snapshot.documents.lastOrNull()?.id)
    }

    suspend fun update(item: T, id: String) {
        docRef.document(id).set(item).await()
        cache[id] = CachedData(item)
    }

    suspend fun delete(id: String) {
        docRef.document(id).delete().await()
        cache.remove(id)
    }

    suspend fun runQuery(builder: FirestoreQueryBuilder): Collection<FirestoreDocument<T>> {
        val built = builder.buildQuery(docRef)
        var query = built.query
        if (built.startAfterDocId != null) {
            val doc = docRef.document(built.startAfterDocId).get().await()
            if (doc.exists()) query = query.startAfter(doc)
        }
        val snapshot = query.get().await()
        val now = System.currentTimeMillis()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(classType)?.let {
                cache[doc.id] = CachedData(it, now)
                FirestoreDocument(doc.id, it)
            }
        }
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
