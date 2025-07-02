package com.github.walkandtag.ui.pages

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.walkandtag.ui.components.EmptyFeed
import com.github.walkandtag.ui.components.FeedPathEntry
import com.github.walkandtag.ui.components.LoadingScreen
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.HomeState
import com.github.walkandtag.ui.viewmodel.HomeViewModel
import com.github.walkandtag.util.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun Home(
    nav: Navigator = koinInject(),
    globalViewModel: GlobalViewModel = koinInject(),
    viewModel: HomeViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is HomeState.Loading -> {
            LoadingScreen()
        }

        is HomeState.Error -> {
            val message = (uiState as HomeState.Error).message
            globalViewModel.showSnackbar(message)
        }

        is HomeState.Success -> {
            val items = (uiState as HomeState.Success).items
            if (items.isNotEmpty()) {
                LazyColumn {
                    items(items) { feedItem ->
                        FeedPathEntry(
                            user = feedItem.first,
                            path = feedItem.second,
                            onProfileClick = { nav.navigate(Navigation.Profile(feedItem.first.id)) },
                            onPathClick = { nav.navigate(Navigation.PathDetails(feedItem.second.id)) },
                            onFavoritePathClick = { /* @TODO: Add path to favorites */ })
                    }
                }
            } else {
                EmptyFeed()
            }
        }
    }
}