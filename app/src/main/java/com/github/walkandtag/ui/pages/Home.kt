package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.github.walkandtag.ui.components.FeedPathEntry
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.HomeState
import com.github.walkandtag.ui.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun Home(
    nav: NavHostController,
    globalViewModel: GlobalViewModel = koinInject(),
    viewModel: HomeViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is HomeState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is HomeState.Error -> {
            val message = (uiState as HomeState.Error).message
            globalViewModel.showSnackbar(message)
        }

        is HomeState.Success -> {
            val items = (uiState as HomeState.Success).items
            LazyColumn {
                items(items) { feedItem ->
                    FeedPathEntry(
                        nav = nav, user = feedItem.first, path = feedItem.second
                    )
                }
            }
        }
    }
}