package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.walkandtag.ui.components.FeedPathEntry
import com.github.walkandtag.ui.viewmodel.HomeState
import com.github.walkandtag.ui.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun Home(viewModel: HomeViewModel = koinViewModel()) {

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is HomeState.Loading -> {
            // Replace with your own fancy loading UI
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is HomeState.Error -> {
            val message = (uiState as HomeState.Error).message
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $message")
            }
        }

        is HomeState.Success -> {
            val items = (uiState as HomeState.Success).items
            LazyColumn {
                items(items) { feedItem ->
                    FeedPathEntry(
                        username = feedItem.username,
                        length = feedItem.length,
                        duration = feedItem.duration,
                        path = feedItem.points
                    )
                }
            }
        }
    }
}