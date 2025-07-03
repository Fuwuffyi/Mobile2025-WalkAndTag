package com.github.walkandtag

import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.MainNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize MapLibre
        MapLibre.getInstance(this, null, WellKnownTileServer.MapTiler)
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun BuildNavbar(currentPage: Navigation, onPageChange: (Navigation) -> Unit) {
        val auth = koinInject<Authentication>()
        val homeNavbar =
            NavbarBuilder().addButton(Navigation.Settings, Icons.Filled.Settings, "Settings")
                .addButton(Navigation.Home, Icons.Filled.Home, "Home")
        // If logged in, add account button
        if (auth.getCurrentUserId() != null) {
            homeNavbar.addButton(
                Navigation.Profile(auth.getCurrentUserId() ?: "NULL"),
                Icons.Filled.AccountCircle,
                "Account"
            )
        }
        homeNavbar.Navbar(currentPage, onPageChange)
    }

    @Composable
    override fun NavigationContent(navController: androidx.navigation.NavHostController) {
        MainNavGraph(navController)
    }

    override fun navbarQualifier() = named("main")
}

