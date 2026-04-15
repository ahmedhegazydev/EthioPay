package com.eathioPay.demo.payment.ver_id_2
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity

@Composable
fun FaceLoginButton(
    onLoggedIn: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔵 Icon Circle
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Face ID",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Biometric Login",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Use Face ID or Fingerprint to continue securely",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔥 Main Button
                Button(
                    onClick = {
                        authenticatePreferFace(
                            activity = activity,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Authentication successful ✅",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoggedIn()
                            },
                            onFail = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onNeedEnroll = {
                                Toast.makeText(
                                    context,
                                    "Please enroll Face ID or Fingerprint first 👆",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Biometrics")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Face ID will be used when available,\notherwise Fingerprint",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
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
    val bm = BiometricManager.from(activity)
    val canBio = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

    // ✅ لو الجهاز فيه Face hardware ومفيش أي Biometric متسجل -> ودّيه Enroll
    if (deviceHasFace(activity) && canBio == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
        onNeedEnroll?.invoke()
        openBiometricEnroll(activity, BiometricManager.Authenticators.BIOMETRIC_STRONG)
        onFail("No Face/Fingerprint enrolled. Please enroll and try again.")
        return
    }

    // ✅ افتح الـ Prompt (النظام هيحاول Face، ولو مش متاح هيسمح Fingerprint)
    authenticateWithBiometric(
        activity = activity,
        title = "Face ID Login",
        subtitle = "Look at the camera (Face ID). Use fingerprint if needed.",
        onSuccess = onSuccess,
        onFail = onFail,
        onNeedEnroll = {
            onNeedEnroll?.invoke()
        },
        allowDeviceCredential = false // Face/Fingerprint فقط
    )
}