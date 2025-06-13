package com.github.walkandtag.ui.pages

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Login(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Login")
        Row {
            Button(
                modifier = Modifier.weight(1.0f),
                onClick = onLogin
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton (
                modifier = Modifier.weight(1.0f),
                onClick = onRegister
            ) {
                Text("Register")
            }
        }
    }
}