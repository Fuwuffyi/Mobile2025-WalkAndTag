package com.github.walkandtag

import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.HomeViewModel
import com.github.walkandtag.ui.viewmodel.LoginViewModel
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import com.github.walkandtag.ui.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // Firebase singleton
    single { FirebaseAuth.getInstance() }
    // Authentication singleton
    single { Authentication(get()) }
    // Repository singletons
    single<FirestoreRepository<UserSchema>>(named("users")) {
        FirestoreRepository.create("users")
    }
    single<FirestoreRepository<PathSchema>>(named("paths")) {
        FirestoreRepository.create("paths")
    }
    // View models
    single { GlobalViewModel() } // Singleton per evitare di passarlo a tutte le pagine
    viewModel(named("login")) { NavbarViewModel("login") }
    viewModel(named("main")) { NavbarViewModel("home") }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get(), get(named("users"))) }
    viewModel { HomeViewModel(get(named("paths")), get(named("users"))) }
}