package com.github.walkandtag.ui.components

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.walkandtag.MainActivity
import com.github.walkandtag.R
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun GoogleButton(scope: CoroutineScope) {
    val auth = koinInject<Authentication>()
    val context = LocalContext.current
    FloatingActionButton(
        onClick = {
            scope.launch {
                when (auth.loginWithGoogle(context)) {
                    is AuthResult.Success -> {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }

                    is AuthResult.Failure -> {
                        Toast.makeText(
                            context, "Could not login using google", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }) {
        Icon(
            painter = painterResource(R.drawable.google_logo),
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
            contentDescription = "Google login"
        )
    }
}