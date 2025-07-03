package com.github.walkandtag.ui.pages

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.walkandtag.AuthActivity
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.ui.components.LanguageDialog
import com.github.walkandtag.ui.components.MaterialIconInCircle
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

enum class Languages {
    System, Italiano, English
}

@Composable
fun Settings(globalViewModel: GlobalViewModel = koinInject()) {
    // @TODO(): Implement settings viewModel (for all functionality)
    var showLanguageDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authentication = koinInject<Authentication>()
    val userRepo = koinInject<FirestoreRepository<UserSchema>>(named("users"))
    val theme = globalViewModel.themeState.collectAsStateWithLifecycle()
    val lang = globalViewModel.languageState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        // Placeholder section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Filled.Person)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Placeholder", style = MaterialTheme.typography.bodyLarge)
        }

        // Appearance section
        Text("Appearance", style = MaterialTheme.typography.titleLarge)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Default.PhoneAndroid)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Use system mode", style = MaterialTheme.typography.bodyLarge)
            }
            Switch(
                checked = theme.value.theme == Theme.System, onCheckedChange = {
                    globalViewModel.toggleSystemTheme()
                })
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Filled.DarkMode)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
            }
            Switch(
                checked = theme.value.theme == Theme.Dark, onCheckedChange = {
                    globalViewModel.toggleTheme()
                }, enabled = theme.value.theme != Theme.System
            )
        }

        // Regional section
        Text("Regional", style = MaterialTheme.typography.titleLarge)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLanguageDialog = true }) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Default.GTranslate)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Language: ${lang.value.lang.name}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        if (showLanguageDialog) {
            LanguageDialog(
                currentLanguage = lang.value.lang,
                onLanguageSelected = { globalViewModel.setLang(it) },
                onDismiss = { showLanguageDialog = false }
            )
        }

        // Logout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .clickable {
                    authentication.logout()
                    val intent = Intent(context, AuthActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(
                    Modifier.size(36.dp),
                    icon = Icons.AutoMirrored.Filled.Logout,
                    colorBack = MaterialTheme.colorScheme.error,
                    colorFront = MaterialTheme.colorScheme.errorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout", style = MaterialTheme.typography.bodyLarge)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        // Delete account
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    runBlocking {
                        val userId = authentication.getCurrentUserId()!!
                        userRepo.delete(userId)
                        authentication.deleteCurrentUser()
                        authentication.logout()
                        val intent = Intent(context, AuthActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }
                }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(
                    Modifier.size(36.dp),
                    icon = Icons.Filled.Delete,
                    colorBack = MaterialTheme.colorScheme.error,
                    colorFront = MaterialTheme.colorScheme.errorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Erase Account", style = MaterialTheme.typography.bodyLarge)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
