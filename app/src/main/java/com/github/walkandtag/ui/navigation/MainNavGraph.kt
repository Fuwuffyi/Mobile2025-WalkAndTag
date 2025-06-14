package com.github.walkandtag.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.walkandtag.AuthActivity
import com.github.walkandtag.auth.Authentication
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainNavGraph() {
    val ctx = LocalContext.current
    val authentication = remember { Authentication(FirebaseAuth.getInstance()) }

    val navigationController = rememberNavController()

    NavHost(navController = navigationController, startDestination = "home") {
        composable(
            "home"
        ) {
            Scaffold { pad ->
                Button(
                    onClick = {
                        authentication.logout()
                        val intent = Intent(ctx, AuthActivity::class.java)
                        ctx.startActivity(intent)
                        (ctx as? Activity)?.finish()
                    },
                    modifier = Modifier
                        .padding(pad)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) { Text("Logout") }
            }
        }
    }
}
