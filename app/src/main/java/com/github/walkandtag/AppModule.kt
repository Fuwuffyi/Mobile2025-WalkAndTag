package com.github.walkandtag

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.BiometricRepository
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.repository.LanguageRepository
import com.github.walkandtag.repository.SavedPathRepository
import com.github.walkandtag.repository.ThemeRepository
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.HomeViewModel
import com.github.walkandtag.ui.viewmodel.LoginViewModel
import com.github.walkandtag.ui.viewmodel.NavbarViewModel
import com.github.walkandtag.ui.viewmodel.PathDetailsViewModel
import com.github.walkandtag.ui.viewmodel.ProfileViewModel
import com.github.walkandtag.ui.viewmodel.RegisterViewModel
import com.github.walkandtag.ui.viewmodel.SettingsViewModel
import com.github.walkandtag.util.Navigator
import com.github.walkandtag.util.Notifier
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // Datastore
    single<DataStore<Preferences>> {
        val context: Context = get()
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") })
    }
    // Firebase singleton
    single { FirebaseAuth.getInstance() }
    // Authentication singleton
    single { Authentication(get()) }
    // Utility singletons
    single<Navigator> { Navigator() }
    single<Notifier> { Notifier(androidContext()) }
    // Repository singletons
    single<ThemeRepository> { ThemeRepository(get()) }
    single<LanguageRepository> { LanguageRepository(get()) }
    single<BiometricRepository> { BiometricRepository(get()) }
    single<SavedPathRepository> { SavedPathRepository() }
    single<FirestoreRepository<UserSchema>>(named("users")) {
        FirestoreRepository.create("users")
    }
    single<FirestoreRepository<PathSchema>>(named("paths")) {
        FirestoreRepository.create("paths")
    }
    // View models
    single { GlobalViewModel(get(), get(), get()) } // Singleton per evitare di ricrearlo per tutte le pagine
    viewModel(named("login")) { NavbarViewModel(Navigation.Login) }
    viewModel(named("main")) { NavbarViewModel(Navigation.Home) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get(), get(named("users"))) }
    viewModel { SettingsViewModel(get(), get(named("users"))) }
    viewModel { HomeViewModel(get(named("paths")), get(named("users"))) }
    viewModel { ProfileViewModel(get(), get(named("users")), get(named("paths")), get()) }
    viewModel { PathDetailsViewModel(get(named("users")), get(named("paths"))) }
}
