package com.github.walkandtag.ui.pages

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.ui.components.FeedPathEntry
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun Home() {
    // @TODO(), sistemare in un viewModel?
    val pathRepo: FirestoreRepository<PathSchema> = koinInject(named("paths"))
    val userRepo: FirestoreRepository<UserSchema> = koinInject(named("users"))

    LazyColumn {
        runBlocking {
            val paths = pathRepo.getAll().toList()
            val userIds = paths.map { it.data.userId }.toSet()
            val users = userRepo.get(userIds).associate { it.id to it.data }

            items(items = paths) { path ->
                val user = users[path.data.userId] ?: UserSchema("Deleted User")
                FeedPathEntry(
                    user.username, path.data.length, path.data.time, path.data.points
                )
            }
        }
    }
}