package com.github.walkandtag

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.theme.WalkAndTagTheme
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.NavbarEvent
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import com.github.walkandtag.util.Navigator
import com.github.walkandtag.util.updateLocale
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.Qualifier

abstract class BaseActivity : FragmentActivity() {
    protected val globalViewModel by inject<GlobalViewModel>()

    override fun attachBaseContext(newBase: Context) {
        // Set base language as sytstem
        val updatedContext = newBase.updateLocale(null)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // @TODO(), Should I move this to a viewmodel??? Unsure
            val globalState by globalViewModel.globalState.collectAsStateWithLifecycle()
            // Update language
            val context = remember(globalState.language) {
                baseContext.updateLocale(globalState.language.locale)
            }
            CompositionLocalProvider(
                LocalContext provides context,
                LocalActivity provides this,
                LocalActivityResultRegistryOwner provides this
            ) {
                WalkAndTagTheme(theme = globalState.theme) {
                    // Get navbar stuff
                    val navigator: Navigator = koinInject()
                    val navController = rememberNavController()
                    navigator.setController(navController)
                    val navbarViewModel =
                        koinViewModel<NavbarViewModel>(qualifier = navbarQualifier())
                    val navbarState by navbarViewModel.uiState.collectAsState()
                    // Handle navbar events
                    LaunchedEffect(Unit) {
                        navbarViewModel.events.collectLatest { event ->
                            if (event is NavbarEvent.NavigateTo) {
                                navigator.navigate(event.route)
                            }
                        }
                    }
                    // Main page content
                    Scaffold(
                        snackbarHost = { SnackbarHost(globalViewModel.snackbarHostState) },
                        floatingActionButton = {
                            FloatingActionButtonContent()
                        },
                        bottomBar = {
                            BuildNavbar(navbarState.currentPage) { page ->
                                navbarViewModel.onChangePage(page)
                            }
                        }) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            NavigationContent(navController)
                        }
                    }
                }
            }
        }
    }

    @Composable
    abstract fun BuildNavbar(currentPage: Navigation, onPageChange: (Navigation) -> Unit)

    @Composable
    open fun FloatingActionButtonContent() {
    }

    @Composable
    abstract fun NavigationContent(navController: androidx.navigation.NavHostController)

    abstract fun navbarQualifier(): Qualifier
}
