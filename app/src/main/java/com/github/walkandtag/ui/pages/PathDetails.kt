package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.walkandtag.firebase.db.Filter
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.ui.components.StaticMapFavorite
import com.github.walkandtag.ui.components.StaticMapPath
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun PathDetails(pathId: String) {

    // @TODO(): Move to viewModel
    val pathRepo = koinInject<FirestoreRepository<PathSchema>>(named("paths"))
    val userRepo = koinInject<FirestoreRepository<UserSchema>>(named("users"))
    var path: FirestoreDocument<PathSchema>? = null
    var publisher: FirestoreDocument<UserSchema>? = null
    runBlocking {
        path = pathRepo.get(pathId)
        if (path != null) publisher = userRepo.get(path.data.userId)
    }

    if (path != null && publisher != null) {
        Column {
            Text(text = path.data.name)
            Text(text = "Author: ${publisher.data.username}")
            Text(text = "Length: ${path.data.length}")
            Text(text = "Time: ${path.data.time}")
            StaticMapFavorite(
                path = path.data.points,
                modifier = Modifier.fillMaxWidth(),
                onPathClick = { /* @TODO: Redirect to full screen map */ })
        }
    }
}