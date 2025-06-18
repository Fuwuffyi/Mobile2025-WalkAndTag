package com.github.walkandtag.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.walkandtag.ui.pages.Home
import com.github.walkandtag.ui.pages.Profile
import com.github.walkandtag.ui.pages.Settings

@Composable
fun MainNavGraph(navigationController: NavHostController) {
    NavHost(navController = navigationController, startDestination = "home") {
        composable(
            "settings",
        ) { Settings() }

        composable(
            "home"
        ) { Home() }

        composable(
            "profile",
        ) { Profile() }
    }
}
