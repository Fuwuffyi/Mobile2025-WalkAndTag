package com.github.walkandtag.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.walkandtag.R
import com.mapbox.mapboxsdk.geometry.LatLng

@Composable
fun StaticMapFavorite(
    modifier: Modifier = Modifier,
    path: Collection<LatLng>,
    onPathClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(3f / 2f)
            .clickable(onClick = onPathClick)
    ) {
        StaticMapPath(
            path = path, modifier = Modifier.matchParentSize()
        )
        IconButton(
            onClick = onFavoriteClick,
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
