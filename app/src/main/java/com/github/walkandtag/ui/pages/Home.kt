package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.walkandtag.ui.components.EmptyFeed
import com.github.walkandtag.ui.components.FeedPathEntry
import com.github.walkandtag.ui.components.LoadingScreen
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.HomeState
import com.github.walkandtag.ui.viewmodel.HomeViewModel
import com.github.walkandtag.util.Navigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun Home(
    nav: Navigator = koinInject(),
    globalViewModel: GlobalViewModel = koinInject(),
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Filters & Search",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                // @TODO: Add filter/search UI here
            }
        }) {
        Column(Modifier.fillMaxSize()) {
            // Simple header with filter button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    scope.launch { drawerState.open() }
                }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Open Filters")
                }
            }

            // Content area
            Box(Modifier.weight(1f)) {
                when (uiState) {
                    is HomeState.Loading -> LoadingScreen()
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
                                        onFavoritePathClick = { /* TODO: Add to favorites */ },
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        } else {
                            EmptyFeed()
                        }
                    }
                }
            }
        }
    }
}
