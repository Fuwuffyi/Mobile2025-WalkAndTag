package com.github.walkandtag.firebase.auth

sealed class AuthResult {
    data object Success : AuthResult()
    data class Failure(val exception: Throwable?) : AuthResult()
}
