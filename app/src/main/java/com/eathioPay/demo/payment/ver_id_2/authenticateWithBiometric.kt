package com.eathioPay.demo.payment.ver_id_2

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun authenticateWithBiometric(
    activity: FragmentActivity,
    title: String = "Login",
    subtitle: String = "Try Face ID first, then Fingerprint",
    onSuccess: () -> Unit,
    onFail: (String) -> Unit,
    onNeedEnroll: (() -> Unit)? = null,
    allowDeviceCredential: Boolean = false, // خليها false لو عايز Biometric بس
) {
    val bm = BiometricManager.from(activity)

    val authenticators =
        if (allowDeviceCredential) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

    when (bm.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> Unit

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            onNeedEnroll?.invoke()
            openBiometricEnroll(activity, authenticators)
            onFail("No Face/Fingerprint enrolled. Please enroll and try again.")
            return
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            onFail("This device doesn't support biometrics.")
            return
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            onFail("Biometric hardware unavailable. Try again.")
            return
        }

        else -> {
            onFail("Biometric not available.")
            return
        }
    }

    val executor = ContextCompat.getMainExecutor(activity)

    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                onFail("Not recognized")
            }

//            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                onFail(errString.toString())
//            }
override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    val isTooManyAttempts =
        errorCode == BiometricPrompt.ERROR_LOCKOUT ||
                errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT

    if (isTooManyAttempts) {
        // ✅ fallback to device credential (PIN/Pattern/Password)
        authenticateWithDeviceCredential(
            activity = activity,
            title = "Confirm it's you",
            subtitle = "Enter your phone password / PIN",
            onSuccess = onSuccess,
            onFail = onFail
        )
        return
    }

    onFail(errString.toString())
}

        }
    )

    val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(authenticators)

    if (!allowDeviceCredential) {
        builder.setNegativeButtonText("Cancel")
    }

    prompt.authenticate(builder.build())
}


 fun openBiometricEnroll(activity: FragmentActivity, authenticators: Int) {
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, authenticators)
            }
        } else {
            // fallback للأقدم
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }

    activity.startActivity(intent)
}


fun authenticateWithDeviceCredential(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFail: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)

    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFail(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onFail("Not recognized")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        // ✅ ده اللي بيخليها تفتح PIN/Password
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    prompt.authenticate(promptInfo)
}

fun launchDeviceCredentialFallback(
    activity: Activity,
    requestCode: Int = 1010
) {
    val km = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val intent = km.createConfirmDeviceCredentialIntent(
        "Confirm it's you",
        "Enter PIN/Password"
    )
    if (intent != null) activity.startActivityForResult(intent, requestCode)
}
