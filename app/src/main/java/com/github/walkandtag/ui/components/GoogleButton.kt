package com.github.walkandtag.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.walkandtag.R

@Composable
fun GoogleButton(registerCallback: () -> Unit) {
    FloatingActionButton(onClick = registerCallback) {
        Icon(
            painter = painterResource(R.drawable.google_logo),
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
            contentDescription = "Google login"
        )
    }
}