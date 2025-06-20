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
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.MainNavGraph
import com.github.walkandtag.ui.theme.WalkAndTagTheme
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

private val homeNavbar: NavbarBuilder = NavbarBuilder()
    .addButton("settings", Icons.Filled.Settings)
    .addButton("home", Icons.Filled.Home)
    .addButton("profile", Icons.Filled.AccountCircle)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(
            this,
            null,
            WellKnownTileServer.MapTiler
        )
        enableEdgeToEdge()
        setContent {
            WalkAndTagTheme {
                val navigator = rememberNavController()
                val viewModel = koinViewModel<NavbarViewModel>(qualifier = named("main"))
                val state by viewModel.uiState.collectAsState()

                Scaffold(
                    bottomBar = {
                        homeNavbar.Navbar(state.currentPage) {
                            viewModel.onChangePage(it, navController = navigator)
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        MainNavGraph(navigator)
                    }
                }
            }
        }
    }
}