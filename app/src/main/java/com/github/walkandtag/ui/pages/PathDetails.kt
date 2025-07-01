package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.github.walkandtag.ui.components.StaticMapFavorite
import com.github.walkandtag.ui.viewmodel.PathDetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PathDetails(pathId: String, viewModel: PathDetailsViewModel = koinViewModel()) {

    LaunchedEffect(pathId) {
        viewModel.loadData(pathId)
    }

    val state = viewModel.uiState.collectAsState()

    if (state.value.path != null) {
        Column {
            Text(text = "Name: ${state.value.path!!.data.name}")
            Text(text = "Author: ${state.value.publisher?.data?.username ?: "Account Deleted"}")
            Text(text = "Length: ${state.value.path!!.data.length}")
            Text(text = "Time: ${state.value.path!!.data.time}")
            StaticMapFavorite(
                path = state.value.path!!.data.points,
                modifier = Modifier.fillMaxWidth(),
                onPathClick = { /* @TODO: Redirect to full screen map */ },
                onFavoriteClick = { /* @TODO: Add path to favorites */ })
        }
    }
}
