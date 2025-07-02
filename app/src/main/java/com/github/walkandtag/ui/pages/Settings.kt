package com.github.walkandtag.ui.pages

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.walkandtag.AuthActivity
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.ui.viewmodel.SettingViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun Settings(viewModel: SettingViewModel = koinViewModel()) {
    val context = LocalContext.current
    val authentication = koinInject<Authentication>()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Cambia tema",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp)
            )
            Row(
                modifier = Modifier
                    .width(100.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { viewModel.setTheme(Theme.Light) }) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = "Imposta modalità chiara",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(70.dp)
                    )
                }
                IconButton(onClick = { viewModel.setTheme(Theme.Dark) }) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Imposta modalità scura",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Modifica profilo",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp)
            )
            Box(
                modifier = Modifier
                    .width(100.dp)
            ) {
                IconButton(
                    onClick = { /* Azione */ },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica account",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Elimina account",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp)
            )
            Box(
                modifier = Modifier
                    .width(100.dp)
            ) {
                IconButton(
                    onClick = { /* Azione */ },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina account",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(150.dp))
        Button(
            onClick = {
                authentication.logout()
                val intent = Intent(context, AuthActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(30.dp)
        ) {
            Text(
                "Logout",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
