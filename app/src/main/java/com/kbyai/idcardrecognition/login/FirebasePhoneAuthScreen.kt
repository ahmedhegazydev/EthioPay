package com.kbyai.idcardrecognition.login

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kbyai.idcardrecognition.BuildConfig
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kbyai.idcardrecognition.noble.ui.components.NobleInputField
import com.kbyai.idcardrecognition.noble.ui.components.NoblePrimaryButton
import com.kbyai.idcardrecognition.noble.ui.components.NobleTextAction
import com.kbyai.idcardrecognition.payment.stripe.findActivity
import org.koin.androidx.compose.koinViewModel

@Composable
fun FirebasePhoneAuthScreen(
    onLoginSuccess: (prismaUserId: String) -> Unit,
) {
    val vm: AuthViewModel = koinViewModel()
    val st by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scrollState = rememberScrollState()
    var handledUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var autoSubmittedOtp by rememberSaveable(st.verificationId) { mutableStateOf<String?>(null) }

    LaunchedEffect(st.prismaUserId) {
        val prismaUserId = st.prismaUserId ?: return@LaunchedEffect
        if (handledUserId == prismaUserId) return@LaunchedEffect
        handledUserId = prismaUserId
        onLoginSuccess(prismaUserId)
    }

    LaunchedEffect(st.verificationId, st.otp, st.isVerifying, st.isCompletingLogin) {
        if (st.otp.length < 6) {
            autoSubmittedOtp = null
            return@LaunchedEffect
        }

        if (
            st.verificationId != null &&
            !st.isVerifying &&
            !st.isCompletingLogin &&
            autoSubmittedOtp != st.otp
        ) {
            autoSubmittedOtp = st.otp
            vm.onEvent(AuthEvent.VerifyOtp)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            LoginHeader(
                isOtpStep = st.verificationId != null,
                onBack = { vm.onEvent(AuthEvent.BackToPhone) },
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (st.verificationId == null) {
                            "Login by Phone"
                        } else {
                            "Enter Verification Code"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                    )

                    Text(
                        text = if (st.verificationId == null) {
                            "Enter your phone number to get started."
                        } else {
                            "We sent a 6-digit code to ${st.phone}."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (st.verificationId == null) {
                    NobleInputField(
                        value = st.phone,
                        onValueChange = { vm.onEvent(AuthEvent.PhoneChanged(it)) },
                        label = "Phone Number",
                        placeholder = "+251 912 345 678",
                        leadingIcon = Icons.Default.PhoneAndroid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    NoblePrimaryButton(
                        text = "Send OTP",
                        onClick = { vm.onEvent(AuthEvent.SendOtp(activity)) },
                        enabled = st.resendSecondsLeft == 0,
                        isLoading = st.isSendingOtp,
                        leadingIcon = Icons.AutoMirrored.Filled.Send,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    )

                    Text(
                        text = "Your information is secure and encrypted.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    NobleInputField(
                        value = st.otp,
                        onValueChange = { vm.onEvent(AuthEvent.OtpChanged(it)) },
                        label = "Verification Code",
                        placeholder = "Enter 6-digit code",
                        leadingIcon = Icons.Default.CheckCircle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    NoblePrimaryButton(
                        text = "Verify & Continue",
                        onClick = { vm.onEvent(AuthEvent.VerifyOtp) },
                        enabled = st.otp.length == 6,
                        isLoading = st.isVerifying || st.isCompletingLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    )

                    NobleTextAction(
                        text = if (st.resendSecondsLeft > 0) {
                            "Resend in ${formatTimer(st.resendSecondsLeft)}"
                        } else {
                            "Resend Code"
                        },
                        leadingIcon = if (st.resendSecondsLeft > 0) null else Icons.Default.Refresh,
                        onClick = { vm.onEvent(AuthEvent.SendOtp(activity)) },
                        enabled = !st.isSendingOtp && st.resendSecondsLeft == 0,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (BuildConfig.SIMULATE_OTP) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Sim OTP: 123456") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!st.message.isNullOrBlank()) {
                Snackbar(
                    action = {
                        TextButton(onClick = { vm.onEvent(AuthEvent.ClearMessage) }) {
                            Text("OK")
                        }
                    },
                ) {
                    Text(st.message.orEmpty())
                }
            }
        }
    }
}

@Composable
private fun LoginHeader(
    isOtpStep: Boolean,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isOtpStep) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

private fun formatTimer(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
