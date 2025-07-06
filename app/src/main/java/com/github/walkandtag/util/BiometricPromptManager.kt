package com.github.walkandtag.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricStatus {
    SUCCESS, NO_HARDWARE, NO_ENROLLED, FAILURE,
}

class BiometricPromptManager(private val activity: FragmentActivity) {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun authenticate(onSuccess: () -> Unit, onFail: () -> Unit, onError: (String) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)

        biometricPrompt = BiometricPrompt(
            activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFail()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Authentication")
            .setSubtitle("Authenticate using your fingerprint or face")
            .setNegativeButtonText("Cancel").build()

        biometricPrompt.authenticate(promptInfo)
    }
}

fun checkBiometricAvailability(context: Context): BiometricStatus {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.SUCCESS
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.NO_HARDWARE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NO_ENROLLED
        else -> BiometricStatus.FAILURE
    }
}
