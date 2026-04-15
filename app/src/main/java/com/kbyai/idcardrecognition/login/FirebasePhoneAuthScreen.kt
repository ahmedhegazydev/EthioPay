package com.kbyai.idcardrecognition.login

import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kbyai.idcardrecognition.payment.demo.saveAuthData

@Composable
fun FirebasePhoneAuthScreen(
    onLoginSuccess: (prismaUserId: String) -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val st by vm.state.collectAsState()
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    fun mmss(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    LaunchedEffect(st.isLoggedIn, st.userId) {
        val userId = st.userId ?: return@LaunchedEffect
        if (!st.isLoggedIn) return@LaunchedEffect

        saveAuthData(
            context = ctx,
            prismaUserId = userId,
            phone = st.normalizedPhone ?: st.phone,
        )
        onLoginSuccess(userId)
    }

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
                        onClick = { clipboard.setText(AnnotatedString("123456")) },
                        label = { Text("OTP: 123456") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFFF7ED),
                            labelColor = Color(0xFFB45309)
                        )
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = st.phone,
                    onValueChange = { vm.onEvent(AuthEvent.PhoneChanged(it)) },
                    label = { Text("Phone") },
                    placeholder = { Text("010xxxxxxx or +2010xxxxxxx") },
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val otpBtnEnabled = !st.isSendingOtp && !st.isVerifying && st.resendSecondsLeft == 0
                Button(
                    onClick = { vm.onEvent(AuthEvent.SendOtp) },
                    enabled = otpBtnEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (st.isSendingOtp) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Sending…", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(
                            if (st.canResend) Icons.Default.Refresh else Icons.Default.Send,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (st.canResend) "Resend OTP" else "Send OTP",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (st.resendSecondsLeft > 0) {
                    Text(
                        "You can resend in ${mmss(st.resendSecondsLeft)}",
                        color = textSub,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (st.otpRequested) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = st.otp,
                        onValueChange = { vm.onEvent(AuthEvent.OtpChanged(it)) },
                        label = { Text("OTP") },
                        placeholder = { Text("6 digits") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = { vm.onEvent(AuthEvent.VerifyOtp) },
                        enabled = !st.isVerifying && st.otp.length == 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (st.isVerifying) {
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

        if (st.message != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { vm.onEvent(AuthEvent.ClearMessage) }) { Text("OK") }
                }
            ) { Text(st.message!!) }
        }

        if (st.isLoggedIn) {
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
                clipboard.setText(AnnotatedString(st.phone))
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
