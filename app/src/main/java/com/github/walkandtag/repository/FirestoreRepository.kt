package com.github.walkandtag.repository

import android.util.Log
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.FirestoreQueryBuilder
import com.github.walkandtag.firebase.db.PagedResult
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty1

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
        cache[ref.id] = CachedData(item)
        return ref.id
    }

    suspend fun get(id: String): FirestoreDocument<T>? {
        cache[id]?.let { cached ->
            if (System.currentTimeMillis() - cached.timestamp < expiryMillis) {
                return FirestoreDocument(id, cached.data)
            } else {
                cache.remove(id)
            }
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
        if (fetch.isEmpty()) return cachedDocs
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
        return processSnapshot(snapshot)
    }

    suspend fun getAllPaged(limit: UInt = 15u, startAfterId: String? = null): PagedResult<T> {
        var query = docRef.limit(limit.toLong())
        if (startAfterId != null) {
            val doc = docRef.document(startAfterId).get().await()
            if (doc.exists()) query = query.startAfter(doc)
        }
        val snapshot = query.get().await()
        val docs = processSnapshot(snapshot)
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

    suspend fun delete(ids: Collection<String>) {
        ids.forEach { id ->
            docRef.document(id).delete().await()
            cache.remove(id)
        }
    }

    suspend fun query(builder: FirestoreQueryBuilder<T>.() -> Unit): Collection<FirestoreDocument<T>> {
        val queryBuilder = FirestoreQueryBuilder(classType)
        queryBuilder.builder()
        return runQuery(queryBuilder)
    }

    suspend fun queryPaged(
        limit: UInt = 15u,
        startAfterId: String? = null,
        builder: FirestoreQueryBuilder<T>.() -> Unit
    ): PagedResult<T> {
        val queryBuilder = FirestoreQueryBuilder(classType)
        queryBuilder.builder()
        queryBuilder.limit(limit.toLong())
        startAfterId?.let { queryBuilder.startAfter(it) }
        val docs = runQuery(queryBuilder)
        return PagedResult(docs, docs.lastOrNull()?.id)
    }

    suspend fun <V> findBy(property: KProperty1<T, V>, value: V): Collection<FirestoreDocument<T>> {
        return query { equalTo(property, value) }
    }

    suspend fun <V> findFirst(property: KProperty1<T, V>, value: V): FirestoreDocument<T>? {
        return query {
            equalTo(property, value)
            limit(1)
        }.firstOrNull()
    }

    suspend fun <V> findByIn(
        property: KProperty1<T, V>, values: List<V>
    ): Collection<FirestoreDocument<T>> {
        return query { whereIn(property, values) }
    }

    suspend fun <V> findByRange(
        property: KProperty1<T, V>, min: V? = null, max: V? = null, ascending: Boolean = true
    ): Collection<FirestoreDocument<T>> {
        return query {
            min?.let { greaterThanOrEqualTo(property, it) }
            max?.let { lessThanOrEqualTo(property, it) }
            orderBy(property, ascending)
        }
    }

    suspend fun runQuery(builder: FirestoreQueryBuilder<T>): Collection<FirestoreDocument<T>> {
        val built = builder.buildQuery(docRef)
        var query = built.query
        if (built.startAfterDocId != null) {
            val doc = docRef.document(built.startAfterDocId).get().await()
            if (doc.exists()) query = query.startAfter(doc)
        }
        val snapshot = query.get().await()
        return processSnapshot(snapshot)
    }

    private fun processSnapshot(snapshot: com.google.firebase.firestore.QuerySnapshot): Collection<FirestoreDocument<T>> {
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
            collectionPath: String,
            firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
            expiryMillis: Long = TimeUnit.MINUTES.toMillis(60)
        ): FirestoreRepository<T> {
            return FirestoreRepository(
                firestore.collection(collectionPath), T::class.java, expiryMillis
            )
        }
    }
}
