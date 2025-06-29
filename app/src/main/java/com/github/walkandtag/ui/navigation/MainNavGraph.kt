package com.github.walkandtag.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.walkandtag.ui.pages.Home
import com.github.walkandtag.ui.pages.PathDetails
import com.github.walkandtag.ui.pages.Profile
import com.github.walkandtag.ui.pages.Settings

// @TODO(): Maybe find way to move navigation controllers to koin
@Composable
fun MainNavGraph(navigationController: NavHostController) {
    NavHost(navController = navigationController, startDestination = Navigation.Home) {
        composable<Navigation.Settings> { Settings() }

        composable<Navigation.Home> { Home(navigationController) }

        composable<Navigation.Profile> {
            val routeData: Navigation.Profile = it.toRoute()
            Profile(userId = routeData.userId)
        }

        composable<Navigation.PathDetails> {
            val routeData: Navigation.PathDetails = it.toRoute()
            PathDetails(routeData.pathId)
        }
    }
}
