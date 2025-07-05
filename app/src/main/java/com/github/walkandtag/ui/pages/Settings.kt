package com.github.walkandtag.ui.pages

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
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
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.walkandtag.AuthActivity
import com.github.walkandtag.R
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.repository.Language
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.ui.components.DialogBuilder
import com.github.walkandtag.ui.components.MaterialIconInCircle
import com.github.walkandtag.ui.viewmodel.GlobalViewModel
import com.github.walkandtag.util.rememberMultiplePermissions
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import java.util.EnumSet

@Composable
fun Settings(globalViewModel: GlobalViewModel = koinInject()) {
    // @TODO(): Implement settings viewModel (for all functionality)
    var showLanguageDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authentication = koinInject<Authentication>()
    val userRepo = koinInject<FirestoreRepository<UserSchema>>(named("users"))
    val globalState = globalViewModel.globalState.collectAsStateWithLifecycle()
    val name = runBlocking { userRepo.get(authentication.getCurrentUserId()!!) }

    /*
    val hasBiometricPermission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
            // API 28+ → USE_BIOMETRIC
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.USE_BIOMETRIC
            ) == PackageManager.PERMISSION_GRANTED
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            // API 23–27 → USE_FINGERPRINT
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.USE_FINGERPRINT
            ) == PackageManager.PERMISSION_GRANTED
        }

        else -> {
            // Versioni più vecchie → non supportato
            false
        }
    }
*/
    val languageDialog = DialogBuilder(
        title = stringResource(R.string.choose_language),
        onDismiss = { showLanguageDialog = false }) {
        val langStr = it["lang"]!!
        globalViewModel.setLang(Language.valueOf(langStr))
        showLanguageDialog = false
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

        // Placeholder section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Filled.Person)
            Spacer(modifier = Modifier.width(12.dp))
            Text(name!!.data.username, style = MaterialTheme.typography.bodyLarge)
        }

        // Appearance section
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
                    stringResource(R.string.system_mode),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = globalState.value.theme == Theme.System, onCheckedChange = {
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
                Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.bodyLarge)
            }
            Switch(
                checked = globalState.value.theme == Theme.Dark, onCheckedChange = {
                    globalViewModel.toggleTheme()
                }, enabled = globalState.value.theme != Theme.System
            )
        }

        // Regional section
        Text(stringResource(R.string.regional), style = MaterialTheme.typography.titleLarge)

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
                    text = "${stringResource(R.string.language)}: ${globalState.value.language.name}",
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
            languageDialog.Dialog()
        }

        // Se manca il permesso → chiedi
        /*
        // Va messo qui?
        if (!hasBiometricPermission) {
            val permissions = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> arrayOf(Manifest.permission.USE_BIOMETRIC)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> arrayOf(Manifest.permission.USE_FINGERPRINT)
                else -> emptyArray()
            }

            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    context,
                    permissions,
                    1003 // requestCode
                )
            }
        } else {
            // Permesso già concesso → procedi con biometrico
        }
        */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIconInCircle(Modifier.size(36.dp), icon = Icons.Default.Fingerprint)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.biometric_access),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = globalState.value.enabledBiometric,
                onCheckedChange = {
                    globalViewModel.toggleBiometricEnabled()
                },
                enabled = true // hasBiometricPermission
            )
        }

        /*
        val locationPermissionHandler = rememberMultiplePermissions(
            permissions = listOf(Manifest.permission.USE_BIOMETRIC, Manifest.permission.USE_FINGERPRINT)
        ) { status ->
            if (status.values.any { it.isGranted }) {
                val biometricManager = BiometricManager.from(this)
            } else {

            }
        }
        */

        // Logout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .clickable {
                    globalViewModel.setBiometricEnabled(false)
                    authentication.logout()
                    context.startActivity(Intent(context, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
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
                Text(stringResource(R.string.logout), style = MaterialTheme.typography.bodyLarge)
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
                    globalViewModel.setBiometricEnabled(false)
                    runBlocking {
                        val userId = authentication.getCurrentUserId()!!
                        userRepo.delete(userId)
                        authentication.deleteCurrentUser()
                        authentication.logout()
                        context.startActivity(Intent(context, AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
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
                Text(
                    stringResource(R.string.delete_account),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
