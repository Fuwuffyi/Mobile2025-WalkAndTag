package com.github.walkandtag.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.github.walkandtag.ui.components.NavbarBuilder

val homeNavbarBuilder: NavbarBuilder = NavbarBuilder()
    .addButton("settings", Icons.Filled.Settings)
    .addButton("home", Icons.Filled.Home)
    .addButton("profile", Icons.Filled.AccountCircle)

@Composable
fun Home(navController: NavController) {
    Scaffold(
        bottomBar = { homeNavbarBuilder.Navbar(navController, "home") }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text("Home")
        }
    }
}