package com.github.walkandtag.util

import androidx.navigation.NavHostController
import com.github.walkandtag.ui.navigation.Navigation

class Navigator {
    private var navController: NavHostController? = null

    fun setController(navController: NavHostController) {
        this.navController = navController
    }

    fun navigate(route: Navigation) {
        navController?.navigate(route)
            ?: throw IllegalStateException("Navigator was used before navController was set")
    }
}
