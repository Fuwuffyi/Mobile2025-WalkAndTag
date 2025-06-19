package com.github.walkandtag.ui.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.walkandtag.ui.components.FeedPathEntry

@Composable
fun Home() {
    LazyColumn {
        item {
            FeedPathEntry("Giorgio", 1.3f, 1.0f)
        }
        item {
            FeedPathEntry("Marco Pasta", 5.0f, 3.0f)
        }
        item {
            FeedPathEntry("PavoJ", 0.2f, 15.0f)
        }
    }
}