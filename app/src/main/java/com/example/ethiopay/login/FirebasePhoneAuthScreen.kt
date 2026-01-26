package com.example.ethiopay.login


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ethiopay.login.AuthViewModel
import com.example.ethiopay.login.AuthEvent

@Composable
fun FirebasePhoneAuthScreen(
    vm: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val st by vm.state.collectAsState()
    val ctx = LocalContext.current
    val activity = remember(ctx) { ctx as Activity }

    val state by vm.state.collectAsState()

    // 👇 ده المهم
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Login by Phone", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = st.phone,
            onValueChange = { vm.onEvent(AuthEvent.PhoneChanged(it)) },
            label = { Text("Phone (مثال: 010xxxxxxx أو +2010xxxxxxx)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.onEvent(AuthEvent.SendOtp(activity)) },
            enabled = !st.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (st.canResend) "Resend OTP" else "Send OTP") }

        if (st.verificationId != null) {
            OutlinedTextField(
                value = st.otp,
                onValueChange = { vm.onEvent(AuthEvent.OtpChanged(it)) },
                label = { Text("OTP (6 digits)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.onEvent(AuthEvent.VerifyOtp) },
                enabled = !st.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Verify") }
        }

        if (st.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (st.message != null) {
            Snackbar(
                action = { TextButton(onClick = { vm.onEvent(AuthEvent.ClearMessage) }) { Text("OK") } }
            ) { Text(st.message!!) }
        }

        if (st.isLoggedIn) {
            Text("✅ Logged in", style = MaterialTheme.typography.titleMedium)
        }
    }
}
