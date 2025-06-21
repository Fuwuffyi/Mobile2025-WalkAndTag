package com.github.walkandtag.ui.pages

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import com.github.walkandtag.ui.components.FeedPathEntry
import org.maplibre.android.geometry.LatLng

@Composable
fun Home() {
    LazyColumn {
        item {
            FeedPathEntry(
                "Giorgio", 1.3f, 1.0f, listOf(
                    LatLng(44.147421, 12.235026), LatLng(44.148680, 12.234758), LatLng(44.148807, 12.236034)
                )
            )
        }
        item {
            FeedPathEntry(
                "Marco Pasta", 5.0f, 3.0f, listOf(
                    LatLng(44.147421, 12.235026), LatLng(44.148680, 12.234758), LatLng(44.148807, 12.236034)
                )
            )
        }
        item {
            FeedPathEntry(
                "PavoJ", 0.2f, 15.0f, listOf(
                    LatLng(44.396682, 12.211217),
                    LatLng(44.396396, 12.211323),
                    LatLng(44.396495, 12.212089),
                    LatLng(44.394027, 12.213154)
                )
            )
        }
        item {
            FeedPathEntry(
                "Fabio", 15.0f, 2.0f, listOf(
                    LatLng(44.147421, 12.235026), LatLng(44.148680, 12.234758), LatLng(44.148807, 12.236034)
                )
            )
        }
    }
}