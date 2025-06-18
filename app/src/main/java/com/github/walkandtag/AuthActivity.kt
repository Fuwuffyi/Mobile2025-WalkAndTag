package com.github.walkandtag

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.ui.components.GoogleButton
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.LoginNavGraph
import com.github.walkandtag.ui.theme.WalkAndTagTheme
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

private val authNavbar: NavbarBuilder = NavbarBuilder()
    .addButton("login", Icons.AutoMirrored.Filled.Login)
    .addButton("register", Icons.Filled.AssignmentInd)

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalkAndTagTheme {
                val navigator = rememberNavController()
                val scope: CoroutineScope = rememberCoroutineScope()
                val viewModel = koinViewModel<NavbarViewModel>(qualifier = named("login"))
                val state by viewModel.uiState.collectAsState()

                Scaffold(
                    floatingActionButton = { GoogleButton(scope) },
                    bottomBar = {
                        authNavbar.Navbar(state.currentPage) {
                            viewModel.onChangePage(it, navController = navigator)
                        }
                    }
                )
                { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        LoginNavGraph(navigator)
                    }
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            gotoHome()
        }
    }

    private fun gotoHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}