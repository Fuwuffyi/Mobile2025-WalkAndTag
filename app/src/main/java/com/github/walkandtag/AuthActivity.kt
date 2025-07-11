package com.github.walkandtag

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.walkandtag.repository.BiometricRepository
import com.github.walkandtag.ui.components.GoogleButton
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.LoginNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

class AuthActivity : BaseActivity() {

    @Composable
    override fun BuildNavbar(currentPage: Navigation, onPageChange: (Navigation) -> Unit) {
        NavbarBuilder().addButton(Navigation.Login, Icons.AutoMirrored.Filled.Login, "Login")
            .addButton(Navigation.Register, Icons.Filled.AssignmentInd, "Register")
            .Navbar(currentPage, onPageChange)
    }

    @Composable
    override fun FloatingActionButtonContent() {
        val context = LocalContext.current
        val authViewModel: AuthViewModel = koinViewModel()
        GoogleButton {
            authViewModel.loginWithGoogle(context)
        }
    }

    @Composable
    override fun NavigationContent(navController: NavHostController) {
        val biometricState by globalViewModel.biometricEnabled.collectAsStateWithLifecycle()
        val authViewModel: AuthViewModel = koinViewModel()
        val context = LocalContext.current
        val alreadyNavigated = remember { mutableStateOf(false) }
        LaunchedEffect(biometricState) {
            if (!alreadyNavigated.value) {
                when (biometricState) {
                    is BiometricRepository.BiometricPreferenceState.Loaded -> {
                        alreadyNavigated.value = true
                        if ((biometricState as BiometricRepository.BiometricPreferenceState.Loaded).enabled) {
                            authViewModel.checkBiometricAndNavigate(this@AuthActivity)
                        } else if (FirebaseAuth.getInstance().currentUser != null) {
                            context.startActivity(Intent(context, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }
                    }

                    else -> {}
                }
            }
        }
        LaunchedEffect(Unit) {
            authViewModel.uiEvent.collectLatest { event ->
                if (event is AuthViewModel.AuthUIEvent.NavigateToMain) {
                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
        }
        LoginNavGraph(navController)
    }

    override fun navbarQualifier() = named("auth_navbar")
}
