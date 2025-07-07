package com.github.walkandtag.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.walkandtag.ui.pages.FullMap
import com.github.walkandtag.ui.pages.Home
import com.github.walkandtag.ui.pages.PathDetails
import com.github.walkandtag.ui.pages.Profile
import com.github.walkandtag.ui.pages.Settings

@Composable
fun MainNavGraph(navigationController: NavHostController) {
    NavHost(
        navController = navigationController,
        startDestination = Navigation.Home,
        enterTransition = { getEnterTransition(initialState, targetState) },
        exitTransition = { getExitTransition(initialState, targetState) },
        popEnterTransition = { getPopEnterTransition(initialState, targetState) },
        popExitTransition = { getPopExitTransition(initialState, targetState) }) {
        composable<Navigation.Home> { Home() }
        composable<Navigation.Settings> { Settings() }
        composable<Navigation.Profile> {
            val route = it.toRoute<Navigation.Profile>()
            Profile(userId = route.userId)
        }
        composable<Navigation.PathDetails> {
            val route = it.toRoute<Navigation.PathDetails>()
            PathDetails(pathId = route.pathId)
        }
        composable<Navigation.FullMap> {
            val route = it.toRoute<Navigation.FullMap>()
            FullMap(pathId = route.pathId)
        }
    }
}

private const val slideDuration = 300
private const val scaleDuration = 250

private val bottomNavOrder = listOf(
    Navigation.Settings::class.qualifiedName!!,
    Navigation.Home::class.qualifiedName!!,
    Navigation.Profile::class.qualifiedName!!
)

private fun getSlideDirection(from: NavBackStackEntry, to: NavBackStackEntry): SlideDirection {
    val fromIndex = bottomNavOrder.indexOfFirst { from.destination.route?.startsWith(it) == true }
    val toIndex = bottomNavOrder.indexOfFirst { to.destination.route?.startsWith(it) == true }

    return if (fromIndex < toIndex) SlideDirection.LEFT else SlideDirection.RIGHT
}

private enum class SlideDirection {
    LEFT, RIGHT
}

fun getEnterTransition(from: NavBackStackEntry, to: NavBackStackEntry): EnterTransition {
    return when {
        isBottomNavTransition(from, to) -> {
            when (getSlideDirection(from, to)) {
                SlideDirection.LEFT -> slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(slideDuration)
                )

                SlideDirection.RIGHT -> slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(slideDuration)
                )
            }
        }

        isScaleTransition(to) -> scaleIn(animationSpec = tween(scaleDuration))
        else -> fadeIn(animationSpec = tween(150))
    }
}

fun getExitTransition(from: NavBackStackEntry, to: NavBackStackEntry): ExitTransition {
    return when {
        isBottomNavTransition(from, to) -> {
            when (getSlideDirection(from, to)) {
                SlideDirection.LEFT -> slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(slideDuration)
                )

                SlideDirection.RIGHT -> slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(slideDuration)
                )
            }
        }

        isScaleTransition(from) -> scaleOut(animationSpec = tween(scaleDuration))
        else -> fadeOut(animationSpec = tween(150))
    }
}

fun getPopEnterTransition(
    from: NavBackStackEntry, to: NavBackStackEntry
): EnterTransition {
    return getEnterTransition(from, to)
}

fun getPopExitTransition(
    from: NavBackStackEntry, to: NavBackStackEntry
): ExitTransition {
    return getExitTransition(from, to)
}

private fun isBottomNavTransition(from: NavBackStackEntry, to: NavBackStackEntry): Boolean {
    val bottomNavPrefixes = listOf(
        Navigation.Home::class.qualifiedName!!,
        Navigation.Profile::class.qualifiedName!!,
        Navigation.Settings::class.qualifiedName!!
    )
    val fromIsBottom =
        bottomNavPrefixes.any { prefix -> from.destination.route?.startsWith(prefix) == true }
    val toIsBottom =
        bottomNavPrefixes.any { prefix -> to.destination.route?.startsWith(prefix) == true }
    return fromIsBottom && toIsBottom
}

private fun isScaleTransition(entry: NavBackStackEntry): Boolean {
    val scalePrefixes = listOf(
        Navigation.PathDetails::class.qualifiedName!!, Navigation.FullMap::class.qualifiedName!!
    )
    return scalePrefixes.any { prefix ->
        entry.destination.route?.startsWith(prefix) == true
    }
}
