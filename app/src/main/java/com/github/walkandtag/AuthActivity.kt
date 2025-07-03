package com.github.walkandtag

import android.app.Activity
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.ui.components.GoogleButton
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.navigation.LoginNavGraph
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
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
        // Get global view model
        val globalViewModel: GlobalViewModel = koinInject()
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
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
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
        LoginNavGraph(navController)
    }

    override fun navbarQualifier() = named("login")

    override fun onStart() {
        super.onStart()
        // Startup firebase authenticator
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
