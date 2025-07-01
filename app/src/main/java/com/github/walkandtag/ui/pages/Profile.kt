package com.github.walkandtag.ui.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.walkandtag.firebase.db.Filter
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.ui.components.FeedPathEntry
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun Profile(userId: String) {

    // @TODO(): Move to viewModel
    val userRepo = koinInject<FirestoreRepository<UserSchema>>(named("users"))
    val pathRepo = koinInject<FirestoreRepository<PathSchema>>(named("paths"))
    var currentUser: FirestoreDocument<UserSchema>? = null
    var paths: Collection<FirestoreDocument<PathSchema>>? = null
    runBlocking {
        currentUser = userRepo.get(userId)
        if (currentUser != null) paths =
            pathRepo.getFiltered(listOf(Filter("userId", currentUser.id)))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle, contentDescription = "Profile picture"
            )
            if (currentUser != null) {
                Text(currentUser.data.username)
            }
        }
        LazyColumn {
            if (paths != null) {
                items(paths.toList()) { path ->
                    // @TODO(): Navigate to the correct path
                    FeedPathEntry(
                        path = path,
                        onPathClick = { Log.i("NAVIGATE", "Profile: Navigate to Path") })
                }
            }
        }
    }
}