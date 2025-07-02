package com.github.walkandtag.util

import androidx.navigation.NavHostController

class Navigator {
    private lateinit var navController: NavHostController

    fun setController(navController: NavHostController) {
        this.navController = navController
    }

    fun navigate(route: Any) {
        navController.navigate(route)
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}