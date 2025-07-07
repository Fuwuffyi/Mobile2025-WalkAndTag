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
import org.koin.core.qualifier.Qualifier

abstract class BaseActivity : FragmentActivity() {
    protected val globalViewModel by inject<GlobalViewModel>()

    override fun attachBaseContext(newBase: Context) {
        val updatedContext = newBase.updateLocale(null)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigator: Navigator by inject()
            val languageState by globalViewModel.language.collectAsStateWithLifecycle()
            val themeState by globalViewModel.theme.collectAsStateWithLifecycle()
            val context = remember(languageState) {
                baseContext.updateLocale(languageState.locale)
            }
            CompositionLocalProvider(
                LocalContext provides context,
                LocalActivity provides this,
                LocalActivityResultRegistryOwner provides this
            ) {
                WalkAndTagTheme(theme = themeState) {
                    val navController = rememberNavController()
                    LaunchedEffect(navController) {
                        navigator.setController(navController)
                    }
                    val navbarViewModel: NavbarViewModel =
                        koinViewModel(qualifier = navbarQualifier())
                    val navbarState by navbarViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        navbarViewModel.events.collectLatest { event ->
                            if (event is NavbarEvent.NavigateTo) {
                                navigator.navigate(event.route)
                            }
                        }
                    }
                    Scaffold(
                        snackbarHost = { SnackbarHost(globalViewModel.snackbarHostState) },
                        floatingActionButton = { FloatingActionButtonContent() },
                        bottomBar = {
                            BuildNavbar(navbarState.currentPage) { page ->
                                navbarViewModel.onChangePage(page)
                            }
                        }) { paddingValues ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
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
