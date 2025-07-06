package com.github.walkandtag

import android.content.Intent
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.ui.components.GoogleButton
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.LoginNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.util.BiometricPromptManager
import com.github.walkandtag.util.BiometricStatus
import com.github.walkandtag.util.checkBiometricAvailability
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
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
        val coroutineScope = rememberCoroutineScope()
        val auth: Authentication = koinInject()
        val userRepo: FirestoreRepository<UserSchema> = koinInject(qualifier = named("users"))

        GoogleButton {
            coroutineScope.launch {
                when (auth.loginWithGoogle(context)) {
                    is AuthResult.Success -> {
                        userRepo.create(
                            UserSchema(username = auth.getCurrentUserName().orEmpty()),
                            auth.getCurrentUserId().orEmpty()
                        )
                        context.startActivity(Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }

                    is AuthResult.Failure -> {
                        globalViewModel.showSnackbar("Could not login using Google")
                    }
                }
            }
        }
    }

    @Composable
    override fun NavigationContent(navController: androidx.navigation.NavHostController) {
        val globalState by globalViewModel.globalState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val alreadyNavigated = remember { mutableStateOf(false) }
        LaunchedEffect(globalState.enabledBiometric) {
            if (alreadyNavigated.value) return@LaunchedEffect
            alreadyNavigated.value = true
            val auth = FirebaseAuth.getInstance()
            if (globalState.enabledBiometric) {
                when (checkBiometricAvailability(this@AuthActivity)) {
                    BiometricStatus.SUCCESS -> {
                        BiometricPromptManager(this@AuthActivity).authenticate(
                            onSuccess = {
                            if (auth.currentUser != null) {
                                context.startActivity(
                                    Intent(
                                        context, MainActivity::class.java
                                    ).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                finish()
                            }
                        },
                            onFail = { Log.e("Biometric", "Authentication failed") },
                            onError = { Log.e("Biometric", "Error: $it") })
                    }

                    BiometricStatus.NO_ENROLLED -> globalViewModel.showSnackbar("No biometric enrolled.")
                    BiometricStatus.NO_HARDWARE -> globalViewModel.showSnackbar("No biometric hardware.")
                    BiometricStatus.FAILURE -> globalViewModel.showSnackbar("Biometric error.")
                }
            } else if (auth.currentUser != null) {
                context.startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
        LoginNavGraph(navController)
    }

    override fun navbarQualifier() = named("login")
}
