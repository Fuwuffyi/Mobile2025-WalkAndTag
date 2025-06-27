package com.github.walkandtag.firebase.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.github.walkandtag.R
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Authentication(private val auth: FirebaseAuth) {
    private suspend fun signInWithCredential(credential: AuthCredential) = runCatching {
        auth.signInWithCredential(credential).awaitResult()
    }.fold({ AuthResult.Success }, { AuthResult.Failure(it) })

    suspend fun loginWithEmail(email: String, password: String) = runCatching {
        auth.signInWithEmailAndPassword(email, password).awaitResult()
    }.fold({ AuthResult.Success }, { AuthResult.Failure(it) })

    suspend fun registerWithEmail(email: String, password: String) = runCatching {
        auth.createUserWithEmailAndPassword(email, password).awaitResult()
    }.fold({ AuthResult.Success }, { AuthResult.Failure(it) })

    suspend fun loginWithGoogle(context: Context): AuthResult = withContext(Dispatchers.Main) {
        try {
            val options = GetSignInWithGoogleOption.Builder(
                serverClientId = context.getString(R.string.server_client_id)
            ).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(options).build()
            val result = CredentialManager.create(context).getCredential(context, request)
            val custom = result.credential as? CustomCredential
                ?: throw IllegalStateException("Unexpected credential type")
            val gid = GoogleIdTokenCredential.createFrom(custom.data)
            val token = gid.idToken
            val cred = GoogleAuthProvider.getCredential(token, null)
            signInWithCredential(cred)
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    fun logout() = auth.signOut()

    fun getCurrentUserId() = auth.currentUser?.uid

    fun getCurrentUserName() = auth.currentUser?.displayName

    suspend fun deleteCurrentUser() = auth.currentUser?.let {
        runCatching { it.delete().awaitResult() }.fold(
                { AuthResult.Success },
                { AuthResult.Failure(it) })
    } ?: AuthResult.Failure(IllegalStateException("No user to delete"))
}

suspend fun <T> Task<T>.awaitResult(): T = try {
    await()
} catch (e: Exception) {
    throw e
}