package com.github.walkandtag

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.MainNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.theme.WalkAndTagTheme
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.NavbarEvent
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import com.github.walkandtag.ui.viewmodel.SettingViewModel
import com.github.walkandtag.util.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(
            this, null, WellKnownTileServer.MapTiler
        )
        enableEdgeToEdge()
        setContent {
            // Initialize global things
            val navigator: Navigator = koinInject()
            val navigatorController = rememberNavController()
            navigator.setController(navigatorController)
            // Setup navbar
            val viewModel = koinViewModel<NavbarViewModel>(qualifier = named("main"))
            val state by viewModel.uiState.collectAsState()
            // Get global view model
            val globalViewModel: GlobalViewModel = koinInject()

            // Get ThemeViewModel
            val themeViewModel = koinViewModel<SettingViewModel>()
            val themeState by themeViewModel.state.collectAsStateWithLifecycle()

            WalkAndTagTheme(
                darkTheme = themeState.theme == Theme.Dark
            ) {

                // @TODO(): Edit this, this code bad
                val auth = koinInject<Authentication>()
                val homeNavbar: NavbarBuilder =
                    NavbarBuilder().addButton(
                        Navigation.Settings,
                        Icons.Filled.Settings,
                        "Settings"
                    )
                        .addButton(Navigation.Home, Icons.Filled.Home, "Home")
                homeNavbar.addButton(
                    Navigation.Profile(auth.getCurrentUserId() ?: "NULL"),
                    Icons.Filled.AccountCircle, "Account"
                )

                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is NavbarEvent.NavigateTo -> navigator.navigate(event.route)
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(globalViewModel.snackbarHostState) },
                    bottomBar = {
                        homeNavbar.Navbar(state.currentPage) {
                            viewModel.onChangePage(it)
                        }
                    }) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        MainNavGraph(navigatorController)
                    }
                }
            }
        }
    }
}
