package com.github.walkandtag.ui.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Register(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Register")
        Row {
            Button(
                modifier = Modifier.weight(1.0f),
                onClick = { Log.i("Register", "Register: Register pressed") }
            ) {
                Text("Register")
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(
                modifier = Modifier.weight(1.0f),
                onClick = { navController.navigate("login") }
            ) {
                Text("Login")
            }
        }
    }
}