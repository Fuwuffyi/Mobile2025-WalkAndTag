package com.github.walkandtag

import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.MainNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.WellKnownTileServer
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize MapLibre
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            this,
            null,
            WellKnownTileServer.MapTiler
        )
    }

    @Composable
    override fun BuildNavbar(currentPage: Navigation, onPageChange: (Navigation) -> Unit) {
        val auth = koinInject<Authentication>()
        val homeNavbar =
            NavbarBuilder().addButton(Navigation.Settings, Icons.Filled.Settings, stringResource(R.string.settings))
                .addButton(Navigation.Home, Icons.Filled.Home, stringResource(R.string.home))
        // If logged in, add account button
        if (auth.getCurrentUserId() != null) {
            homeNavbar.addButton(
                Navigation.Profile(auth.getCurrentUserId()!!),
                Icons.Filled.AccountCircle,
                stringResource(R.string.account)
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

