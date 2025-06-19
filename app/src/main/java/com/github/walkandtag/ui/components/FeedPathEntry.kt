package com.github.walkandtag.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun FeedPathEntry(username: String, length: Float, duration: Float) {
    Column {
        Row {
            Icon(Icons.Filled.SupervisedUserCircle, "Profile Icon")
            Text(username)
        }
        Row {
            Box(modifier = Modifier.fillMaxWidth().height(256.dp).background(Color(150, 255, 200)))
        }
        Row {
            Text("${String.format(Locale.ITALY ,"%.2f", length)}km")
            Icon(Icons.Filled.PinDrop, "Length")
            Icon(Icons.AutoMirrored.Filled.ShowChart, "Duration (h)")
            Text("${String.format(Locale.ITALY ,"%.2f", duration)}h")
        }
    }
}