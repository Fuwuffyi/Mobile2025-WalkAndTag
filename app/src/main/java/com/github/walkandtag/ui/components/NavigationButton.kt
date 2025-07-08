package com.github.walkandtag.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.walkandtag.R
import com.github.walkandtag.intent.GoogleMapsIntent
import com.mapbox.mapboxsdk.geometry.LatLng

@Composable
fun NavigationButton(
    modifier: Modifier = Modifier,
    startPoint: LatLng,
    endPoint: LatLng,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    Button(
        onClick = {
            GoogleMapsIntent.openGoogleMapsNavigation(
                context = context,
                start = startPoint,
                end = endPoint
            )
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.open_navigation),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}