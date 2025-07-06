package com.github.walkandtag.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.walkandtag.R
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.util.getDistanceString
import com.github.walkandtag.util.getTimeString

@Composable
fun FeedPathEntry(
    modifier: Modifier = Modifier,
    user: FirestoreDocument<UserSchema>? = null,
    path: FirestoreDocument<PathSchema>,
    isFavorite: Boolean = false,
    onProfileClick: (() -> Unit)? = null,
    onPathClick: () -> Unit,
    onFavoritePathClick: () -> Unit
) {
    FeedPathEntryLayout(
        modifier = modifier, userSection = user?.let {
        {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onProfileClick?.invoke() }) {
                MaterialIconInCircle(
                    icon = Icons.Filled.SupervisedUserCircle, modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(it.data.username)
            }
        }
    }, pathTitle = path.data.name, mapContent = {
        if (user != null) {
            StaticMapFavorite(
                path = path.data.points,
                modifier = Modifier.fillMaxWidth(),
                onPathClick = onPathClick,
                onFavoriteClick = onFavoritePathClick
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f)
                    .clickable(onClick = onPathClick)
            ) {
                StaticMapPath(path = path.data.points, modifier = Modifier.fillMaxSize())
                IconButton(
                    onClick = onFavoritePathClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .border(2.dp, Color(255, 127, 0))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = stringResource(R.string.favourite),
                        tint = Color(255, 127, 0),
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }, length = path.data.length, time = path.data.time
    )
}

@Composable
private fun FeedPathEntryLayout(
    modifier: Modifier = Modifier,
    userSection: (@Composable (() -> Unit))? = null,
    pathTitle: String,
    mapContent: @Composable () -> Unit,
    length: Double,
    time: Double
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (userSection != null) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                userSection()
                Text(pathTitle)
            }
        } else {
            Text(
                pathTitle, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        mapContent()
        Spacer(modifier = Modifier.height(16.dp))
        PathDetailsRow(length = length, time = time)
    }
}

@Composable
private fun PathDetailsRow(
    length: Double, time: Double
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = getDistanceString(LocalContext.current, length),
                modifier = Modifier.padding(end = 4.dp)
            )
            Icon(Icons.Filled.PinDrop, contentDescription = stringResource(R.string.length))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Timer, contentDescription = stringResource(R.string.duration))
            Text(
                text = getTimeString(time), modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
