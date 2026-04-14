package com.kbyai.idcardrecognition.payment.ver_id_2

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity

@Composable
fun FaceLoginButton(
    onLoggedIn: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(66.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(82.dp),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Secure Biometric Login",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Use your fingerprint or face ID for quick and secure access to your wallet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Button(
                    onClick = {
                        authenticatePreferFace(
                            activity = activity,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Authentication successful",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                onLoggedIn()
                            },
                            onFail = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onNeedEnroll = {
                                Toast.makeText(
                                    context,
                                    "Please enroll Face ID or fingerprint first",
                                    Toast.LENGTH_LONG,
                                ).show()
                            },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text("Continue with Biometrics", fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "Your biometric data is stored securely on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun deviceHasFace(activity: FragmentActivity): Boolean {
    return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)
}

fun authenticatePreferFace(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFail: (String) -> Unit,
    onNeedEnroll: (() -> Unit)? = null,
) {
    val biometricManager = BiometricManager.from(activity)
    val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

    if (deviceHasFace(activity) && canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
        onNeedEnroll?.invoke()
        openBiometricEnroll(activity, BiometricManager.Authenticators.BIOMETRIC_STRONG)
        onFail("No Face/Fingerprint enrolled. Please enroll and try again.")
        return
    }

    authenticateWithBiometric(
        activity = activity,
        title = "Biometric Login",
        subtitle = "Use Face ID or fingerprint to continue.",
        onSuccess = onSuccess,
        onFail = onFail,
        onNeedEnroll = {
            onNeedEnroll?.invoke()
        },
        allowDeviceCredential = false,
    )
}
