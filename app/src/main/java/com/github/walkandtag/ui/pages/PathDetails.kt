package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.walkandtag.R
import com.github.walkandtag.ui.components.LoadingScreen
import com.github.walkandtag.ui.components.StaticMapFavorite
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.PathDetailsViewModel
import com.github.walkandtag.util.Navigator
import com.github.walkandtag.util.getDistanceString
import com.github.walkandtag.util.getTimeString
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PathDetails(
    pathId: String,
    navigator: Navigator = koinInject(),
    viewModel: PathDetailsViewModel = koinViewModel()
) {
    LaunchedEffect(pathId) {
        viewModel.loadData(pathId)
    }
    val state = viewModel.uiState.collectAsState()
    state.value.path?.let { pathWrapper ->
        val path = pathWrapper.data
        val publisher =
            state.value.publisher?.data?.username ?: stringResource(R.string.deleted_account)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = path.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.by_author, publisher),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Length: ${getDistanceString(LocalContext.current, path.length.toDouble())}",
                    fontSize = 14.sp
                )
                Text("Time: ${getTimeString(path.time.toDouble())}", fontSize = 14.sp)
            }
            StaticMapFavorite(
                path = path.points,
                modifier = Modifier.fillMaxWidth(),
                onPathClick = { navigator.navigate(Navigation.FullMap(pathId)) },
                onFavoriteClick = { /* @TODO: Add path to favorites */ })
            HorizontalDivider()
            Text(text = path.description, style = MaterialTheme.typography.bodyLarge)
        }
    } ?: run {
        LoadingScreen()
    }
}
