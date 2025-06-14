package com.github.walkandtag.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class Authentication(private val auth: FirebaseAuth) {
    fun loginEmailPassword(email: String, password: String, callback: (AuthResult) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Login", "Logged in account: $email")
                    callback(AuthResult.Success)
                } else {
                    Log.w("Login", "Login failed", task.exception)
                    callback(AuthResult.Failure(task.exception))
                }
            }
    }

    fun registerEmailPassword(email: String, password: String, callback: (AuthResult) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Register", "Registered account: $email")
                    callback(AuthResult.Success)
                } else {
                    Log.w("Register", "Register failed", task.exception)
                    callback(AuthResult.Failure(task.exception))
                }
            }
    }

    fun logout() = auth.signOut()
}