package com.github.walkandtag.ui.navigation

import android.util.Log
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.ui.pages.Login
import com.github.walkandtag.ui.pages.Register

@Composable
fun NavigationGraph() {
    val navigationController = rememberNavController()

    NavHost(navController = navigationController, startDestination = "login") {
        composable(
            "login",
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { it } }
        ) { Login(navigationController) }

        composable(
            "register",
            enterTransition = { slideInHorizontally { -it } },
            exitTransition = { slideOutHorizontally { -it } }
        ) { Register(navigationController) }
    }
}
