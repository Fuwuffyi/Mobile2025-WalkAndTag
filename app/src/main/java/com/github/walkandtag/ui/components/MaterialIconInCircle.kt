package com.github.walkandtag.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MaterialIconInCircle(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    colorFront: Color = MaterialTheme.colorScheme.inversePrimary,
    colorBack: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colorBack),

        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colorFront
        )
    }
}