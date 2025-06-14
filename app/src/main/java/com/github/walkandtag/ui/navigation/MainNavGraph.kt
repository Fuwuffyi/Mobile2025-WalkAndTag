package com.github.walkandtag.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavGraph() {
    val navigationController = rememberNavController()

    NavHost(navController = navigationController, startDestination = "home") {
        composable(
            "home"
        ) { Text("You are logged in!") }
    }
}
