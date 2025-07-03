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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.ui.components.GoogleButton
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.LoginNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.theme.WalkAndTagTheme
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.NavbarEvent
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import com.github.walkandtag.util.Navigator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
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
            // Get global view model
            val globalViewModel: GlobalViewModel = koinInject()
            val globalState by globalViewModel.globalState.collectAsStateWithLifecycle()
            // Use theme
            WalkAndTagTheme(
                theme = globalState.theme
            ) {
                // @TODO(), Should I move this to a viewmodel??? Unsure
                // Init navigator and navbar
                val navigator: Navigator = koinInject()
                val navController = rememberNavController()
                navigator.setController(navController)
                val navbarViewModel = koinViewModel<NavbarViewModel>(qualifier = named("login"))
                val state by navbarViewModel.uiState.collectAsState()
                // Setup authentication
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val auth = koinInject<Authentication>()
                val userRepo =
                    koinInject<FirestoreRepository<UserSchema>>(qualifier = named("users"))
                // Get navbar events
                LaunchedEffect(Unit) {
                    navbarViewModel.events.collectLatest { event ->
                        when (event) {
                            is NavbarEvent.NavigateTo -> navigator.navigate(event.route)
                        }
                    }
                }
                // Setup actual page content
                Scaffold(snackbarHost = {
                    SnackbarHost(globalViewModel.snackbarHostState)
                }, floatingActionButton = {
                    // @TODO(), Should I move this to a viewmodel??? Unsure
                    GoogleButton {
                        coroutineScope.launch {
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
                        navbarViewModel.onChangePage(it)
                    }
                }) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        LoginNavGraph(navController)
                    }
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // If logged in, bring to home automatically
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
