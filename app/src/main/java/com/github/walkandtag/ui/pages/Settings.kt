package com.github.walkandtag.ui.pages

import android.app.Activity
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.walkandtag.AuthActivity
import com.github.walkandtag.R
import com.github.walkandtag.repository.Language
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.ui.components.DialogBuilder
import com.github.walkandtag.ui.components.MaterialIconInCircle
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.ui.viewmodel.SettingsViewModel
import org.koin.compose.koinInject
import java.util.EnumSet

@Composable
fun Settings(
    globalViewModel: GlobalViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject()
) {
    val context = LocalContext.current
    val globalState = globalViewModel.globalState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val languageDialog = DialogBuilder(
        title = stringResource(R.string.choose_language),
        onDismiss = { settingsViewModel.toggleLanguageDialog(false) }) {
        val langStr = it["lang"]!!
        globalViewModel.setLang(Language.valueOf(langStr))
        settingsViewModel.toggleLanguageDialog(false)
    }.addRadioGroup(
        id = "lang",
        stringResource(R.string.language),
        EnumSet.allOf(Language::class.java).map { it.name },
        globalState.value.language.name
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall)

        // User info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Filled.Person)
            Spacer(modifier = Modifier.width(12.dp))
            Text(settingsState.username, style = MaterialTheme.typography.bodyLarge)
        }
        // Theme
        Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Default.PhoneAndroid)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.system_mode), style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = globalState.value.theme == Theme.System,
                onCheckedChange = { globalViewModel.toggleSystemTheme() })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Filled.DarkMode)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.bodyLarge)
            }
            Switch(
                checked = globalState.value.theme == Theme.Dark,
                onCheckedChange = { globalViewModel.toggleTheme() },
                enabled = globalState.value.theme != Theme.System
            )
        }
        // Language
        Text(stringResource(R.string.regional), style = MaterialTheme.typography.titleLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { settingsViewModel.toggleLanguageDialog(true) }) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Default.GTranslate)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${stringResource(R.string.language)}: ${globalState.value.language.name}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        // Dialog for language selector
        if (settingsState.showLanguageDialog) {
            languageDialog.Dialog()
        }
        // Logout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .clickable {
                    settingsViewModel.logout {
                        context.startActivity(Intent(context, AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        (context as? Activity)?.finish()
                    }
                }) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                MaterialIconInCircle(
                    Modifier.size(36.dp),
                    icon = Icons.AutoMirrored.Filled.Logout,
                    colorBack = MaterialTheme.colorScheme.error,
                    colorFront = MaterialTheme.colorScheme.errorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.logout), style = MaterialTheme.typography.bodyLarge)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        // Delete account
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    settingsViewModel.deleteAccount {
                        context.startActivity(Intent(context, AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        (context as? Activity)?.finish()
                    }
                }) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                MaterialIconInCircle(
                    Modifier.size(36.dp),
                    icon = Icons.Filled.Delete,
                    colorBack = MaterialTheme.colorScheme.error,
                    colorFront = MaterialTheme.colorScheme.errorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.delete_account),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
