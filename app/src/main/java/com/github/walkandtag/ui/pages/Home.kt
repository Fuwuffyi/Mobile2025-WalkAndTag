package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.walkandtag.R
import com.github.walkandtag.ui.components.EmptyFeed
import com.github.walkandtag.ui.components.FeedPathEntry
import com.github.walkandtag.ui.components.LoadingScreen
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.HomeState
import com.github.walkandtag.ui.viewmodel.HomeViewModel
import com.github.walkandtag.ui.viewmodel.SortDirection
import com.github.walkandtag.ui.viewmodel.SortOption
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
    val state by viewModel.state.collectAsState()
    val favorites by viewModel.favoritePathIds.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }.collect { visibleItems ->
            val lastVisibleItem = visibleItems.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount
            if (lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3) {
                viewModel.loadNextPage()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState, gesturesEnabled = false, drawerContent = {
            if (drawerState.isOpen || drawerState.isAnimationRunning) {
                ModalDrawerSheet {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.filter_search_title),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1.0f)
                            )
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    scope.launch { drawerState.close() }
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.button_close_filters)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.padding(8.dp))
                        OutlinedTextField(
                            value = filters.nameQuery,
                            onValueChange = { viewModel.updateFilters { copy(nameQuery = it) } },
                            label = { Text(stringResource(R.string.filter_name)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = filters.authorQuery,
                            onValueChange = { viewModel.updateFilters { copy(authorQuery = it) } },
                            label = { Text(stringResource(R.string.filter_author)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.padding(8.dp))
                        Text(
                            stringResource(
                                R.string.filter_path_length, filters.minLength, filters.maxLength
                            )
                        )
                        RangeSlider(
                            value = filters.minLength.toFloat()..filters.maxLength.toFloat(),
                            onValueChange = {
                                viewModel.updateFilters {
                                    copy(
                                        minLength = it.start.toInt(),
                                        maxLength = it.endInclusive.toInt()
                                    )
                                }
                            },
                            valueRange = 0f..10000f
                        )
                        Spacer(Modifier.padding(8.dp))
                        Text(
                            stringResource(
                                R.string.filter_path_duration, filters.minTime, filters.maxTime
                            )
                        )
                        RangeSlider(
                            value = filters.minTime.toFloat()..filters.maxTime.toFloat(),
                            onValueChange = {
                                viewModel.updateFilters {
                                    copy(
                                        minTime = it.start.toInt(),
                                        maxTime = it.endInclusive.toInt()
                                    )
                                }
                            },
                            valueRange = 0f..1440f
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = filters.showFavorites, onCheckedChange = {
                                    viewModel.updateFilters { copy(showFavorites = it) }
                                })
                            Text(stringResource(R.string.filter_favourite_only))
                        }
                        Spacer(Modifier.padding(8.dp))
                        Text(stringResource(R.string.sort_by))
                        val allSortOptions = SortOption.entries
                        FlowRow {
                            allSortOptions.forEach { option ->
                                FilterChip(
                                    selected = filters.sortOptions[option] != SortDirection.NONE,
                                    onClick = {
                                        viewModel.updateFilters {
                                            val current = sortOptions[option] ?: SortDirection.NONE
                                            val next = current.next()
                                            copy(sortOptions = sortOptions.toMutableMap().apply {
                                                if (next == SortDirection.NONE) remove(option)
                                                else put(option, next)
                                            })
                                        }
                                    },
                                    label = {
                                        val dir = filters.sortOptions[option] ?: SortDirection.NONE
                                        val suffix = when (dir) {
                                            SortDirection.ASC -> " ↑"
                                            SortDirection.DESC -> " ↓"
                                            else -> ""
                                        }
                                        Text(
                                            option.name.lowercase()
                                                .replaceFirstChar { it.uppercase() } + suffix)
                                    })
                            }
                        }
                    }
                }
            }
        }) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    scope.launch { drawerState.open() }
                }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.button_open_filters)
                    )
                }
            }
            // Content area
            Box(Modifier.weight(1f)) {
                when (state) {
                    is HomeState.Loading -> LoadingScreen()
                    is HomeState.Error -> {
                        val message = (state as HomeState.Error).message
                        globalViewModel.showSnackbar(message)
                    }

                    is HomeState.Success -> {
                        val items = (state as HomeState.Success).items
                        if (items.isNotEmpty()) {
                            LazyColumn(state = listState) {
                                items(items) { feedItem ->
                                    FeedPathEntry(
                                        user = feedItem.first,
                                        path = feedItem.second,
                                        isFavorite = favorites.contains(feedItem.second.id),
                                        onProfileClick = { nav.navigate(Navigation.Profile(feedItem.first.id)) },
                                        onPathClick = { nav.navigate(Navigation.PathDetails(feedItem.second.id)) },
                                        onFavoritePathClick = { viewModel.toggleFavorite(feedItem.second.id) },
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
