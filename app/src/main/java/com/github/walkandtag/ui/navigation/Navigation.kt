package com.github.walkandtag.ui.navigation

import kotlinx.serialization.Serializable

sealed class Navigation {

    @Serializable
    object Login : Navigation()

    @Serializable
    object Register : Navigation()

    @Serializable
    object Home : Navigation()

    @Serializable
    object Settings : Navigation()

    @Serializable
    data class Profile(val userId: String) : Navigation()

    @Serializable
    data class PathDetails(val pathId: String) : Navigation()

    @Serializable
    data class FullMap(val pathId: String) : Navigation()

}
