package com.github.walkandtag

import android.content.Intent
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.runtime.Composable
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

private val authNavbar: NavbarBuilder =
    NavbarBuilder().addButton(Navigation.Login, Icons.AutoMirrored.Filled.Login, "Login")
        .addButton(Navigation.Register, Icons.Filled.AssignmentInd, "Register")

class AuthActivity : BaseActivity() {
    @Composable
    override fun BuildNavbar(currentPage: Navigation, onPageChange: (Navigation) -> Unit) {
        authNavbar.Navbar(currentPage, onPageChange)
    }

    @Composable
    override fun FloatingActionButtonContent() {
        // @TODO(), Should I move this to a viewmodel??? Unsure
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val auth = koinInject<Authentication>()
        val userRepo = koinInject<FirestoreRepository<UserSchema>>(qualifier = named("users"))
        GoogleButton {
            coroutineScope.launch {
                when (auth.loginWithGoogle(context)) {
                    is AuthResult.Success -> {
                        userRepo.create(
                            UserSchema(username = auth.getCurrentUserName().orEmpty()),
                            auth.getCurrentUserId().orEmpty()
                        )
                        startActivity(Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    }

                    is AuthResult.Failure -> {
                        globalViewModel.showSnackbar("Could not login using google")
                    }
                }
            }
        }
    }

    @Composable
    override fun NavigationContent(navController: androidx.navigation.NavHostController) {
        val globalState = globalViewModel.globalState.collectAsStateWithLifecycle()
        if (globalState.value.enabledBiometric) {
            // Check if device supports biometric check
            when (checkBiometricAvailability(this)) {
                BiometricStatus.NO_ENROLLED -> globalViewModel.showSnackbar("You have not setup any biometric device on your device.")
                BiometricStatus.NO_HARDWARE -> globalViewModel.showSnackbar("You do not have biometric support on your device.")
                BiometricStatus.FAILURE -> globalViewModel.showSnackbar("Unknown biometric error.")
                BiometricStatus.SUCCESS -> {
                    // Run biometric prompt
                    BiometricPromptManager(this).authenticate(
                        {
                        // Startup firebase authenticator
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            startActivity(Intent(this, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }
                    },
                        { Log.e("Fail", "NavigationContent: Fail") },
                        { Log.e("Error", "NavigationContent: Error $it") })
                }
            }
        }
        // Startup firebase authenticator
        if (FirebaseAuth.getInstance().currentUser != null && !globalState.value.enabledBiometric) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
        LoginNavGraph(navController)
    }

    override fun navbarQualifier() = named("login")
}
