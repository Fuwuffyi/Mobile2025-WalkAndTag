package com.github.walkandtag.ui.pages

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.github.walkandtag.AuthActivity
import com.github.walkandtag.firebase.auth.Authentication
import com.google.firebase.auth.FirebaseAuth
import org.koin.compose.koinInject

@Composable
fun Settings(navController: NavController) {
    val context = LocalContext.current
    val authentication = koinInject<Authentication>()

    Scaffold(
        bottomBar = { homeNavbarBuilder.Navbar(navController, "settings") }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text("Settings")
            Button(
                onClick = {
                    authentication.logout()
                    val intent = Intent(context, AuthActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            ) { Text("Logout") }
        }
    }
}