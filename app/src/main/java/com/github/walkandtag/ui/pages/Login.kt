package com.github.walkandtag.ui.pages

import android.app.Activity
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.GMobiledata
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.walkandtag.MainActivity
import com.github.walkandtag.auth.AuthResult
import com.github.walkandtag.auth.Authentication
import com.github.walkandtag.ui.components.NavbarBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

val loginNavbarBuilder: NavbarBuilder = NavbarBuilder()
    .addButton("login", Icons.AutoMirrored.Filled.Login)
    .addButton("register", Icons.Filled.AssignmentInd)

@Composable
fun Login(navController: NavController) {
    val context = LocalContext.current
    val authentication = remember { Authentication(FirebaseAuth.getInstance()) }
    val scope = rememberCoroutineScope()

    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { loginNavbarBuilder.Navbar(navController, "login") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        when (authentication.loginWithGoogle(context)) {
                            is AuthResult.Success -> {
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                            }

                            is AuthResult.Failure -> {
                                Toast.makeText(
                                    context,
                                    "Could not login using google",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.GMobiledata, "Google login.")
            }
        }
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
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                ElevatedButton(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            scope.launch {
                                when (authentication.loginWithEmail(email, password)) {
                                    is AuthResult.Success -> {
                                        val intent = Intent(context, MainActivity::class.java)
                                        context.startActivity(intent)
                                        (context as? Activity)?.finish()
                                    }

                                    is AuthResult.Failure -> {
                                        Toast.makeText(
                                            context,
                                            "Could not login",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
            }
        }
    }
}