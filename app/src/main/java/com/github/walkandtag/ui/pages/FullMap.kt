package com.github.walkandtag.ui.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.github.walkandtag.ui.components.InteractiveMapPath
import com.github.walkandtag.ui.components.LoadingScreen
import com.github.walkandtag.ui.viewmodel.FullMapViewModel
import org.koin.compose.koinInject

@Composable
fun FullMap(pathId: String, viewModel: FullMapViewModel = koinInject()) {
    val pathState = viewModel.pathState.collectAsState()
    LaunchedEffect(pathId) {
        viewModel.loadPath(pathId)
    }
    if (pathState.value != null) {
        InteractiveMapPath(path = pathState.value!!.points)
    } else {
        LoadingScreen()
    }
}
