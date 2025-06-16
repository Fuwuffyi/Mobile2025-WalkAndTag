package com.github.walkandtag.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.ui.pages.Home
import com.github.walkandtag.ui.pages.Profile
import com.github.walkandtag.ui.pages.Settings

@Composable
fun MainNavGraph() {
    val navigationController = rememberNavController()

    NavHost(navController = navigationController, startDestination = "home") {
        composable(
            "settings",
            enterTransition = { slideInHorizontally { -it } },
            exitTransition = { slideOutHorizontally { -it } }
        ) { Settings(navigationController) }

        composable(
            "home"
        ) { Home(navigationController) }

        composable(
            "profile",
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { it } }
        ) { Profile(navigationController) }
    }
}
