package com.github.walkandtag.firebase.db

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

sealed class QueryOperator {
    object Equal : QueryOperator()
    object NotEqual : QueryOperator()
    object GreaterThan : QueryOperator()
    object GreaterThanOrEqual : QueryOperator()
    object LessThan : QueryOperator()
    object LessThanOrEqual : QueryOperator()
    object ArrayContains : QueryOperator()
    object ArrayContainsAny : QueryOperator()
    object In : QueryOperator()
    object NotIn : QueryOperator()
}

data class FirestoreQueryFilter(
    val field: String,
    val operator: QueryOperator,
    val value: Any,
    val isDocumentId: Boolean = false
)

data class FirestoreOrder(
    val field: String, val ascending: Boolean = true, val isDocumentId: Boolean = false
)

class FirestoreQueryBuilder<T : Any>(private val classType: Class<T>) {
    private val filters = mutableListOf<FirestoreQueryFilter>()
    private val orders = mutableListOf<FirestoreOrder>()
    private var limit: Long? = null
    private var startAfterDocId: String? = null

    fun <V> where(
        property: KProperty1<T, V>, op: QueryOperator, value: V
    ): FirestoreQueryBuilder<T> {
        val fieldName = getFirestoreFieldName(property)
        filters.add(FirestoreQueryFilter(fieldName, op, value as Any))
        return this
    }

    fun whereDocumentId(op: QueryOperator, value: Any): FirestoreQueryBuilder<T> {
        filters.add(FirestoreQueryFilter("", op, value, isDocumentId = true))
        return this
    }

    fun whereDocumentIdEqualTo(id: String): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.Equal, id)

    fun whereDocumentIdNotEqualTo(id: String): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.NotEqual, id)

    fun whereDocumentIdGreaterThan(id: String): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.GreaterThan, id)

    fun whereDocumentIdGreaterThanOrEqualTo(id: String): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.GreaterThanOrEqual, id)

    fun whereDocumentIdLessThan(id: String): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.LessThan, id)

    fun whereDocumentIdLessThanOrEqualTo(id: String): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.LessThanOrEqual, id)

    fun whereDocumentIdIn(ids: List<String>): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.In, ids)

    fun whereDocumentIdNotIn(ids: List<String>): FirestoreQueryBuilder<T> =
        whereDocumentId(QueryOperator.NotIn, ids)

    fun <V> orderBy(
        property: KProperty1<T, V>, ascending: Boolean = true
    ): FirestoreQueryBuilder<T> {
        val fieldName = getFirestoreFieldName(property)
        orders.add(FirestoreOrder(fieldName, ascending))
        return this
    }

    fun orderByDocumentId(ascending: Boolean = true): FirestoreQueryBuilder<T> {
        orders.add(FirestoreOrder("", ascending, isDocumentId = true))
        return this
    }

    fun <V> equalTo(property: KProperty1<T, V>, value: V): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.Equal, value)

    fun <V> notEqualTo(property: KProperty1<T, V>, value: V): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.NotEqual, value)

    fun <V> greaterThan(property: KProperty1<T, V>, value: V): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.GreaterThan, value)

    fun <V> greaterThanOrEqualTo(property: KProperty1<T, V>, value: V): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.GreaterThanOrEqual, value)

    fun <V> lessThan(property: KProperty1<T, V>, value: V): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.LessThan, value)

    fun <V> lessThanOrEqualTo(property: KProperty1<T, V>, value: V): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.LessThanOrEqual, value)

    fun <V> arrayContains(property: KProperty1<T, List<V>>, value: V): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.ArrayContains, value)

    fun <V> arrayContainsAny(
        property: KProperty1<T, List<V>>, values: List<V>
    ): FirestoreQueryBuilder<T> = where(property, QueryOperator.ArrayContainsAny, values)

    fun <V> whereIn(property: KProperty1<T, V>, values: List<V>): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.In, values)

    fun <V> whereNotIn(property: KProperty1<T, V>, values: List<V>): FirestoreQueryBuilder<T> =
        where(property, QueryOperator.NotIn, values)

    fun limit(limit: Long): FirestoreQueryBuilder<T> {
        this.limit = limit
        return this
    }

    fun startAfter(id: String): FirestoreQueryBuilder<T> {
        this.startAfterDocId = id
        return this
    }

    private fun <V> getFirestoreFieldName(property: KProperty1<T, V>): String {
        property.javaField?.getAnnotation(PropertyName::class.java)?.let {
            return it.value
        }
        return property.name
    }

    internal fun buildQuery(base: Query): BuiltFirestoreQuery {
        var query = base
        for (filter in filters) {
            query = when (filter.operator) {
                is QueryOperator.Equal -> {
                    if (filter.isDocumentId) {
                        query.whereEqualTo(FieldPath.documentId(), filter.value)
                    } else {
                        query.whereEqualTo(filter.field, filter.value)
                    }
                }

                is QueryOperator.NotEqual -> {
                    if (filter.isDocumentId) {
                        query.whereNotEqualTo(FieldPath.documentId(), filter.value)
                    } else {
                        query.whereNotEqualTo(filter.field, filter.value)
                    }
                }

                is QueryOperator.GreaterThan -> {
                    if (filter.isDocumentId) {
                        query.whereGreaterThan(FieldPath.documentId(), filter.value)
                    } else {
                        query.whereGreaterThan(filter.field, filter.value)
                    }
                }

                is QueryOperator.GreaterThanOrEqual -> {
                    if (filter.isDocumentId) {
                        query.whereGreaterThanOrEqualTo(FieldPath.documentId(), filter.value)
                    } else {
                        query.whereGreaterThanOrEqualTo(filter.field, filter.value)
                    }
                }

                is QueryOperator.LessThan -> {
                    if (filter.isDocumentId) {
                        query.whereLessThan(FieldPath.documentId(), filter.value)
                    } else {
                        query.whereLessThan(filter.field, filter.value)
                    }
                }

                is QueryOperator.LessThanOrEqual -> {
                    if (filter.isDocumentId) {
                        query.whereLessThanOrEqualTo(FieldPath.documentId(), filter.value)
                    } else {
                        query.whereLessThanOrEqualTo(filter.field, filter.value)
                    }
                }

                is QueryOperator.ArrayContains -> {
                    // Document ID cannot use array operations
                    if (filter.isDocumentId) {
                        throw IllegalArgumentException("Document ID cannot use array operations")
                    }
                    query.whereArrayContains(filter.field, filter.value)
                }

                is QueryOperator.ArrayContainsAny -> {
                    // Document ID cannot use array operations
                    if (filter.isDocumentId) {
                        throw IllegalArgumentException("Document ID cannot use array operations")
                    }
                    query.whereArrayContainsAny(filter.field, filter.value as List<*>)
                }

                is QueryOperator.In -> {
                    if (filter.isDocumentId) {
                        query.whereIn(FieldPath.documentId(), filter.value as List<*>)
                    } else {
                        query.whereIn(filter.field, filter.value as List<*>)
                    }
                }

                is QueryOperator.NotIn -> {
                    if (filter.isDocumentId) {
                        query.whereNotIn(FieldPath.documentId(), filter.value as List<*>)
                    } else {
                        query.whereNotIn(filter.field, filter.value as List<*>)
                    }
                }
            }
        }
        for (order in orders) {
            query = if (order.isDocumentId) {
                query.orderBy(
                    FieldPath.documentId(),
                    if (order.ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING
                )
            } else {
                query.orderBy(
                    order.field,
                    if (order.ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING
                )
            }
        }
        limit?.let { query = query.limit(it) }
        return BuiltFirestoreQuery(query, startAfterDocId)
    }

    data class BuiltFirestoreQuery(
        val query: Query, val startAfterDocId: String?
    )
}
