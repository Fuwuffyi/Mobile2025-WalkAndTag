package com.github.walkandtag.ui.navigation

import kotlinx.serialization.Serializable

sealed class Navigation {
    abstract val route: String

    @Serializable
    object Login : Navigation() {
        override val route = "login"
    }

    @Serializable
    object Register : Navigation() {
        override val route = "register"
    }

    @Serializable
    object Home : Navigation() {
        override val route = "home"
    }

    @Serializable
    object Settings : Navigation() {
        override val route = "settings"
    }

    @Serializable
    data class Profile(val userId: String) : Navigation() {
        override val route = "profile/$userId"
    }

    @Serializable
    data class PathDetails(val pathId: String) : Navigation() {
        override val route = "path/$pathId"
    }

    @Serializable
    data class FullMap(val pathId: String) : Navigation() {
        override val route = "fullmap/$pathId"
    }

    companion object {
        // Parse a route string into a Navigation object
        fun fromRoute(route: String): Navigation? {
            return when {
                route == Login.route -> Login
                route == Register.route -> Register
                route == Home.route -> Home
                route == Settings.route -> Settings
                route.startsWith("profile/") -> {
                    route.removePrefix("profile/").takeIf { it.isNotBlank() }?.let(::Profile)
                }

                route.startsWith("path/") -> {
                    route.removePrefix("path/").takeIf { it.isNotBlank() }?.let(::PathDetails)
                }

                route.startsWith("fullmap/") -> {
                    route.removePrefix("fullmap/").takeIf { it.isNotBlank() }?.let(::FullMap)
                }

                else -> null
            }
        }
    }
}
