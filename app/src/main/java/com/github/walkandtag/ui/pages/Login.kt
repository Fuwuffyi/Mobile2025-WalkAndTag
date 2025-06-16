package com.github.walkandtag.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.ui.components.GoogleButton
import com.github.walkandtag.ui.components.NavbarBuilder
import com.github.walkandtag.ui.viewmodel.LoginViewModel
import org.koin.compose.koinInject

val loginNavbarBuilder: NavbarBuilder = NavbarBuilder()
    .addButton("login", Icons.AutoMirrored.Filled.Login)
    .addButton("register", Icons.Filled.AssignmentInd)

@Composable
fun Login(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = koinInject<Authentication>()
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { loginNavbarBuilder.Navbar(navController, "login") },
        floatingActionButton = { GoogleButton(scope) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(
                    "Login",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 32.dp)
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                ElevatedButton(
                    onClick = { viewModel.onLogin(context, auth) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
            }
        }
    }
}