package com.github.walkandtag

import android.app.Activity
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.ui.components.GoogleButton
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.LoginNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.theme.WalkAndTagTheme
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.NavbarEvent
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

private val authNavbar: NavbarBuilder =
    NavbarBuilder().addButton(Navigation.Login, Icons.AutoMirrored.Filled.Login, "Login")
        .addButton(Navigation.Register, Icons.Filled.AssignmentInd, "Register")

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalkAndTagTheme {
                val navigator = rememberNavController()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val auth = koinInject<Authentication>()
                val userRepo =
                    koinInject<FirestoreRepository<UserSchema>>(qualifier = named("users"))
                val viewModel = koinViewModel<NavbarViewModel>(qualifier = named("login"))
                val globalViewModel: GlobalViewModel = koinInject()
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is NavbarEvent.NavigateTo -> navigator.navigate(event.route)
                        }
                    }
                }

                Scaffold(snackbarHost = {
                    SnackbarHost(globalViewModel.snackbarHostState)
                }, floatingActionButton = {
                    // @TODO(), Should I move this??? Unsure
                    GoogleButton {
                        scope.launch {
                            val result = auth.loginWithGoogle(context)
                            when (result) {
                                is AuthResult.Success -> {
                                    userRepo.create(
                                        UserSchema(
                                            username = auth.getCurrentUserName().orEmpty()
                                        ), auth.getCurrentUserId().orEmpty()
                                    )
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                    (context as? Activity)?.finish()
                                }

                                is AuthResult.Failure -> globalViewModel.showSnackbar("Could not login using google")
                            }
                        }
                    }
                }, bottomBar = {
                    authNavbar.Navbar(state.currentPage) {
                        viewModel.onChangePage(it)
                    }
                }) { innerPadding ->
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