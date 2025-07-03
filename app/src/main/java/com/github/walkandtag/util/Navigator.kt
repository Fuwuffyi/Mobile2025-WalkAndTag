package com.github.walkandtag.util

import androidx.navigation.NavHostController
import com.github.walkandtag.ui.navigation.Navigation

class Navigator {
    private lateinit var navController: NavHostController

    fun setController(navController: NavHostController) {
        this.navController = navController
    }

    fun navigate(route: Navigation) {
        navController.navigate(route)
    }
}
