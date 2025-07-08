package com.github.walkandtag.firebase.db.schemas

import com.google.firebase.firestore.Query

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
    val field: String, val operator: QueryOperator, val value: Any
)

data class FirestoreOrder(
    val field: String, val ascending: Boolean = true
)

class FirestoreQueryBuilder {
    private val filters = mutableListOf<FirestoreQueryFilter>()
    private val orders = mutableListOf<FirestoreOrder>()
    private var limit: Long? = null
    private var startAfterDocId: String? = null

    fun where(field: String, op: QueryOperator, value: Any): FirestoreQueryBuilder {
        filters.add(FirestoreQueryFilter(field, op, value))
        return this
    }

    fun orderBy(field: String, ascending: Boolean = true): FirestoreQueryBuilder {
        orders.add(FirestoreOrder(field, ascending))
        return this
    }

    fun limit(limit: Long): FirestoreQueryBuilder {
        this.limit = limit
        return this
    }

    fun startAfter(id: String): FirestoreQueryBuilder {
        this.startAfterDocId = id
        return this
    }

    internal fun buildQuery(base: Query): BuiltFirestoreQuery {
        var query = base
        for (filter in filters) {
            query = when (filter.operator) {
                is QueryOperator.Equal -> query.whereEqualTo(filter.field, filter.value)
                is QueryOperator.NotEqual -> query.whereNotEqualTo(filter.field, filter.value)
                is QueryOperator.GreaterThan -> query.whereGreaterThan(filter.field, filter.value)
                is QueryOperator.GreaterThanOrEqual -> query.whereGreaterThanOrEqualTo(
                    filter.field, filter.value
                )

                is QueryOperator.LessThan -> query.whereLessThan(filter.field, filter.value)
                is QueryOperator.LessThanOrEqual -> query.whereLessThanOrEqualTo(
                    filter.field, filter.value
                )

                is QueryOperator.ArrayContains -> query.whereArrayContains(
                    filter.field, filter.value
                )

                is QueryOperator.ArrayContainsAny -> query.whereArrayContainsAny(
                    filter.field, filter.value as List<*>
                )

                is QueryOperator.In -> query.whereIn(filter.field, filter.value as List<*>)
                is QueryOperator.NotIn -> query.whereNotIn(filter.field, filter.value as List<*>)
            }
        }
        for (order in orders) {
            query = query.orderBy(
                order.field,
                if (order.ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING
            )
        }
        if (limit != null) {
            query = query.limit(limit!!)
        }
        return BuiltFirestoreQuery(query, startAfterDocId)
    }

    data class BuiltFirestoreQuery(
        val query: Query, val startAfterDocId: String?
    )
}
