package com.eathioPay.demo.feature.auth.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthRoute(
    onLoginSuccess: (String) -> Unit,
    viewModel: AuthFeatureViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateNext -> onLoginSuccess(effect.userId)
            }
        }
    }

    AuthScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
) {
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val pageBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7F8FC), Color(0xFFF2F4FA), Color(0xFFF7F8FC))
    )
    val cardBg = Color.White
    val border = Color(0xFFE5E7EB)
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val accent = Color(0xFF4F46E5)
    val accentSoft = Color(0xFFEEF2FF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoginHeader(
            textMain = textMain,
            textSub = textSub,
            accent = accent,
            accentSoft = accentSoft,
            border = border,
            cardBg = cardBg,
            onCopyOtp = { clipboard.setText(AnnotatedString("123456")) },
        )

        LoginPhoneCard(
            state = state,
            border = border,
            cardBg = cardBg,
            textSub = textSub,
            onIntent = onIntent,
        )

        AnimatedContent(targetState = state.otpRequested, label = "otp_card") { otpRequested ->
            if (otpRequested) {
                OtpCard(
                    state = state,
                    border = border,
                    cardBg = cardBg,
                    onIntent = onIntent,
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }

        AnimatedVisibility(
            visible = state.message != null,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        ) {
            Snackbar(
                action = {
                    TextButton(onClick = { onIntent(AuthIntent.ClearMessage) }) { Text("OK") }
                }
            ) { Text(state.message.orEmpty()) }
        }

        AnimatedVisibility(visible = state.isLoggedIn, enter = fadeIn(), exit = fadeOut()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF047857))
                    Spacer(Modifier.width(10.dp))
                    Text("Logged in", color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                }
            }
        }

        AssistChip(
            onClick = {
                clipboard.setText(AnnotatedString(state.phone))
                Toast.makeText(ctx, "Phone copied", Toast.LENGTH_SHORT).show()
            },
            label = { Text("Copy phone") },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color(0xFFF3F4F6),
                labelColor = textMain,
                leadingIconContentColor = textMain
            )
        )
    }
}

@Composable
private fun LoginHeader(
    textMain: Color,
    textSub: Color,
    accent: Color,
    accentSoft: Color,
    border: Color,
    cardBg: Color,
    onCopyOtp: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentSoft, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = accent)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Login by Phone",
                        color = textMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Backend OTP login with demo code 123456",
                        color = textSub,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                AssistChip(
                    onClick = onCopyOtp,
                    label = { Text("OTP: 123456") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFFFF7ED),
                        labelColor = Color(0xFFB45309)
                    )
                )
            }
        }
    }
}

@Composable
private fun LoginPhoneCard(
    state: AuthUiState,
    border: Color,
    cardBg: Color,
    textSub: Color,
    onIntent: (AuthIntent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.phone,
                onValueChange = { onIntent(AuthIntent.PhoneChanged(it)) },
                label = { Text("Phone") },
                placeholder = { Text("010xxxxxxx or +2010xxxxxxx") },
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { onIntent(AuthIntent.SendOtp) },
                enabled = !state.isSendingOtp && !state.isVerifying && state.resendSecondsLeft == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isSendingOtp) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Sending…", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        if (state.canResend) Icons.Default.Refresh else Icons.Default.Send,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (state.canResend) "Resend OTP" else "Send OTP",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            AnimatedVisibility(visible = state.resendSecondsLeft > 0) {
                Text(
                    "You can resend in ${formatTimer(state.resendSecondsLeft)}",
                    color = textSub,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OtpCard(
    state: AuthUiState,
    border: Color,
    cardBg: Color,
    onIntent: (AuthIntent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.otp,
                onValueChange = { onIntent(AuthIntent.OtpChanged(it)) },
                label = { Text("OTP") },
                placeholder = { Text("6 digits") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { onIntent(AuthIntent.VerifyOtp) },
                enabled = state.canSubmitOtp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isVerifying) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Verifying…", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Verify", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun formatTimer(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreviewIdle() {
    AuthScreen(state = AuthUiState(), onIntent = {})
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreviewOtp() {
    AuthScreen(
        state = AuthUiState(
            otpRequested = true,
            resendSecondsLeft = 35,
            message = "OTP sent. Use 123456 for the demo.",
        ),
        onIntent = {},
    )
}
