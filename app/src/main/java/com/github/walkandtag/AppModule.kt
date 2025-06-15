package com.github.walkandtag

import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.google.firebase.auth.FirebaseAuth
import org.koin.dsl.module

val appModule = module {
    // Firebase singleton
    single { FirebaseAuth.getInstance() }
    // Authentication singleton
    single { Authentication(get()) }
    // User repository singleton
    single<FirestoreRepository<UserSchema>> {
        FirestoreRepository.create("users")
    }
}