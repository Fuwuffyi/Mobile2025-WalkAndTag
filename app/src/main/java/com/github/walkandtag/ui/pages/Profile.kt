package com.github.walkandtag.ui.pages

import android.Manifest
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.walkandtag.R
import com.github.walkandtag.service.PathRecordingService
import com.github.walkandtag.ui.components.DialogBuilder
import com.github.walkandtag.ui.components.EmptyFeed
import com.github.walkandtag.ui.components.FeedPathEntry
import com.github.walkandtag.ui.components.MaterialIconInCircle
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.ProfileViewModel
import com.github.walkandtag.util.Navigator
import com.github.walkandtag.util.rememberMultiplePermissions
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun Profile(
    userId: String,
    nav: Navigator = koinInject(),
    viewModel: ProfileViewModel = koinViewModel(),
    globalViewModel: GlobalViewModel = koinInject()
) {
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
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

    // @TODO(): Clean this up or move to viewModel
    var inDialog by remember { mutableStateOf(false) }
    val locationPermissionHandler = rememberMultiplePermissions(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { status ->
        if (status.values.any { it.isGranted }) {
            if (!state.isRecording) {
                context.startForegroundService(Intent(context, PathRecordingService::class.java))
            } else {
                context.stopService(Intent(context, PathRecordingService::class.java))
                inDialog = true
            }
            viewModel.toggleRecording()
        } else {
            globalViewModel.showSnackbar("You do not have location permissions") // stringResource(R.string.no_location_permissions)
        }
    }
    if (inDialog) {
        DialogBuilder(title = "Path Details", onDismiss = { inDialog = false }, onConfirm = {
            val title = it["Title"]!!
            val description = it["Description"]!!
            if (title.length <= 4) {
                globalViewModel.showSnackbar("Title must contain at least 4 characters.") // stringResource(R.string.error_title)
                return@DialogBuilder
            }
            viewModel.savePath(title, description)
            inDialog = false
        }).addInput("Title").addInput("Description", multiLine = true).Dialog()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(
                    icon = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.propic),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.user?.data?.username ?: stringResource(R.string.deleted_account),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (viewModel.isOwnProfile()) {
                ElevatedButton(
                    onClick = { locationPermissionHandler.launchPermissionRequest() }) {
                    Text(
                        if (state.isRecording) stringResource(R.string.save_path)
                        else stringResource(R.string.rec_path)
                    )
                }
            }
        }
        if (state.paths.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 64.dp),
                state = listState
            ) {
                items(state.paths.toList()) { path ->
                    FeedPathEntry(
                        path = path,
                        onPathClick = { nav.navigate(Navigation.PathDetails(path.id)) },
                        onFavoritePathClick = { /* @TODO: Add path to favorites */ })
                }
            }
        } else {
            EmptyFeed()
        }
    }
}
