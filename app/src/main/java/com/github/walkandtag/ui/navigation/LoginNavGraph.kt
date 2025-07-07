package com.github.walkandtag.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.walkandtag.ui.pages.Login
import com.github.walkandtag.ui.pages.Register

@Composable
fun LoginNavGraph(navigationController: NavHostController) {
    NavHost(navController = navigationController, startDestination = Navigation.Login) {
        composable<Navigation.Login>(
            enterTransition = { slideInHorizontally { -it } },
            exitTransition = { slideOutHorizontally { -it } }) { Login() }

        composable<Navigation.Register>(
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { it } }) { Register() }
    }
}
