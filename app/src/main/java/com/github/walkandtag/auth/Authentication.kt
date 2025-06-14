package com.github.walkandtag.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

val auth: FirebaseAuth = FirebaseAuth.getInstance()

fun loginEmailPassword(email: String, password: String, callback: (Boolean) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Login", "Logged in account: $email")
                callback(true)
            } else {
                Log.w("Login", "Login failed", task.exception)
                callback(false)
            }
        }
}

fun registerEmailPassword(email: String, password: String, callback: (Boolean) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Register", "Registered account: $email")
                callback(true)
            } else {
                Log.w("Register", "Register failed", task.exception)
                callback(false)
            }
        }
}

fun logout() {
    auth.signOut()
}