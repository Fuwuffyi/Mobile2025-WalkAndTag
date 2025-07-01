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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.walkandtag.ui.components.FeedPathEntry
import com.github.walkandtag.ui.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun Profile(userId: String, viewModel: ProfileViewModel = koinViewModel()) {
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    val state = viewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle, contentDescription = "Profile picture"
            )
            state.value.user?.let { Text(it.data.username) }
        }
        LazyColumn {
            if (state.value.paths.isNotEmpty()) {
                items(state.value.paths.toList()) { path ->
                    // @TODO(): Navigate to the correct path
                    FeedPathEntry(
                        path = path,
                        onPathClick = { Log.i("NAVIGATE", "Profile: Navigate to Path") })
                }
            }
        }
    }
}