package com.github.walkandtag.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.android.geometry.LatLng

@Composable
fun StaticMapFavorite(
    path: Collection<LatLng>,
    onPathClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(6f / 4f)
            .clickable(onClick = onPathClick)
    ) {
        StaticMapPath(
            path = path, modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .border(2.dp, Color(255, 127, 0))
        ) {
            Icon(
                imageVector = Icons.Filled.StarBorder,
                contentDescription = "Favorite",
                tint = Color(255, 127, 0),
                modifier = Modifier.size(50.dp)
            )
        }
    }
}
