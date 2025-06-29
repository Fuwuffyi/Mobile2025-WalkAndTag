package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.UserSchema
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun Profile(userId: String) {

    // @TODO(): Move to viewModel
    val userRepo = koinInject<FirestoreRepository<UserSchema>>(named("users"))
    var currentUser: FirestoreDocument<UserSchema>? = null
    runBlocking {
        currentUser = userRepo.get(userId)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle, contentDescription = "Profile picture"
            )
            if (currentUser != null) {
                Text(currentUser.data.username)
            }
        }
    }
}