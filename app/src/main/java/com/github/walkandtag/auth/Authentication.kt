package com.github.walkandtag.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.github.walkandtag.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Authentication(private val auth: FirebaseAuth) {
    private fun signInCredential(credential: AuthCredential, callback: (AuthResult) -> Unit) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Google Login", "Logged in account")
                    callback(AuthResult.Success)
                } else {
                    Log.w("Google Login", "Login failed", task.exception)
                    callback(AuthResult.Failure(task.exception))
                }
            }
    }

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

    fun loginWithGoogle(context: Context, callback: (AuthResult) -> Unit) {
        val signInOption =
            GetSignInWithGoogleOption.Builder(serverClientId = context.getString(R.string.server_client_id))
                .build()
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()
        val credentialManager = CredentialManager.create(context)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val gid = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = gid.idToken
                    val authCred = GoogleAuthProvider.getCredential(idToken, null)
                    signInCredential(authCred, callback)
                } else {
                    throw RuntimeException("Unexpected credential")
                }
            } catch (e: Exception) {
                callback(AuthResult.Failure(e))
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