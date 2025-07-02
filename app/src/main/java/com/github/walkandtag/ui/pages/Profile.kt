package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.walkandtag.ui.components.FeedPathEntry
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

    val state = viewModel.uiState.collectAsState()

    // @TODO(): Unsure if I should clean this up
    val locationPermissionHandler = rememberMultiplePermissions(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { status ->
        if (status.values.any { it.isGranted }) {
            viewModel.toggleRecording()
        } else {
            globalViewModel.showSnackbar("You do not have location permissions")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.AccountCircle, contentDescription = "Profile picture"
                )
                state.value.user?.let { Text(it.data.username) }
            }
            Row {
                if (viewModel.isOwnProfile()) {
                    ElevatedButton(
                        onClick = { locationPermissionHandler.launchPermissionRequest() }) {
                        Text(if (state.value.isRecording) "Save Path" else "Record Path")
                    }
                }
            }
        }
        if (state.value.paths.isNotEmpty()) {
            LazyColumn {
                items(state.value.paths.toList()) { path ->
                    FeedPathEntry(
                        path = path,
                        onPathClick = { nav.navigate(Navigation.PathDetails(path.id)) },
                        onFavoritePathClick = { /* @TODO: Add path to favorites */ })
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("There's nothing here...")
            }
        }
    }
}
