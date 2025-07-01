package com.github.walkandtag.ui.components

import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinDrop
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
import androidx.compose.ui.unit.dp
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import java.util.Locale

// @TODO(): Clean up this code
@Composable
fun FeedPathEntry(
    user: FirestoreDocument<UserSchema>,
    path: FirestoreDocument<PathSchema>,
    onProfileClick: () -> Unit,
    onPathClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.size(40.dp))
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.SupervisedUserCircle, "Profile Icon")
            Text(user.data.username, modifier = Modifier.padding(start = 4.dp))
        }
        Row(
            modifier = Modifier.clickable(onClick = onPathClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(6.0f / 4.0f)
            ) {
                StaticMapPath(
                    path = path.data.points, modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    // @TODO(): Add favorite
                    onClick = { Log.i("TEST", "FeedPathEntry: ") },
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format(Locale.ITALY, "%.2f", path.data.length)}km",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Icon(Icons.Filled.PinDrop, contentDescription = "Length")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, contentDescription = "Duration (h)")
                Text(
                    text = "${String.format(Locale.ITALY, "%.2f", path.data.time)}h",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FeedPathEntry(
    path: FirestoreDocument<PathSchema>, onPathClick: () -> Unit, modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.size(40.dp))
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.clickable(onClick = onPathClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(6.0f / 4.0f)
            ) {
                StaticMapPath(
                    path = path.data.points, modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    // @TODO(): Add favorite
                    onClick = { Log.i("TEST", "FeedPathEntry: ") },
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format(Locale.ITALY, "%.2f", path.data.length)}km",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Icon(Icons.Filled.PinDrop, contentDescription = "Length")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, contentDescription = "Duration (h)")
                Text(
                    text = "${String.format(Locale.ITALY, "%.2f", path.data.time)}h",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}