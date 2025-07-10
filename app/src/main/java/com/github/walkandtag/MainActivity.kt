package com.github.walkandtag

import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
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
        Mapbox.getInstance(this, null, WellKnownTileServer.MapTiler)
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun BuildNavbar(currentPage: Navigation, onPageChange: (Navigation) -> Unit) {
        val auth: Authentication = koinInject()
        val builder = NavbarBuilder().addButton(
            Navigation.Settings, Icons.Default.Settings, stringResource(R.string.settings)
        ).addButton(Navigation.Home, Icons.Default.Home, stringResource(R.string.home))
        auth.getCurrentUserId()?.let { userId ->
            builder.addButton(
                Navigation.Profile(userId),
                Icons.Default.AccountCircle,
                stringResource(R.string.account)
            )
        }
        builder.Navbar(currentPage, onPageChange)
    }

    @Composable
    override fun NavigationContent(navController: NavHostController) {
        MainNavGraph(navController)
    }

    override fun navbarQualifier() = named("main_navbar")
}
