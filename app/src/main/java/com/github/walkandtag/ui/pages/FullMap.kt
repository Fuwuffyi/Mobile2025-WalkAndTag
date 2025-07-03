package com.github.walkandtag.ui.pages

import androidx.compose.runtime.Composable
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.ui.components.InteractiveMapPath
import com.github.walkandtag.ui.components.LoadingScreen
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

// @TODO(): Create a viewModel for this
@Composable
fun FullMap(pathId: String) {
    val pathRepo = koinInject<FirestoreRepository<PathSchema>>(named("paths"))
    var path: PathSchema? = null
    runBlocking {
        path = pathRepo.get(pathId)?.data
    }
    if (path != null) {
        InteractiveMapPath(path = path.points)
    } else {
        LoadingScreen()
    }
}