package com.github.walkandtag.util

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.github.walkandtag.ui.navigation.Navigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class Navigator {
    private val navigationClasses: Collection<KClass<out Navigation>> by lazy {
        Navigation::class.sealedSubclasses
    }
    private var navController: NavHostController? = null
    private val _currentRoute = MutableStateFlow<Navigation?>(null)
    val currentRoute: StateFlow<Navigation?> = _currentRoute.asStateFlow()

    fun setController(navController: NavHostController) {
        this.navController = navController
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val navigation = parseDestinationToNavigation(destination, arguments)
            _currentRoute.value = navigation
        }
    }

    private fun parseDestinationToNavigation(
        destination: NavDestination, arguments: Bundle?
    ): Navigation? {
        // Get route value to build a Navigation object
        val route = destination.route ?: return null
        val routeClass = navigationClasses.find {
            route.contains("Navigation.${it.simpleName}")
        } ?: return null
        // If object has no params, return it
        routeClass.objectInstance?.let { return it }
        // Otherwise, get first constructor and parameters via reflection
        val routeConstructor = routeClass.primaryConstructor ?: return null
        val routeConstructorParams = routeConstructor.parameters
        // Map parameters to constructor
        val argsMap = routeConstructorParams.associateWith { param ->
            val name = param.name ?: return@associateWith null
            arguments?.get(name)
        }
        // Build said object
        return try {
            routeConstructor.callBy(argsMap)
        } catch (e: Exception) {
            Log.e("NAVIGATOR", "Failed to instantiate Navigation route.", e)
            null
        }
    }

    fun navigate(route: Navigation) {
        navController?.navigate(route)
            ?: throw IllegalStateException("Navigator was used before navController was set")
    }
}
