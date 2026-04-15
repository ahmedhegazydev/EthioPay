package com.eathioPay.demo.login

import androidx.compose.runtime.Composable
import com.eathioPay.demo.feature.auth.presentation.AuthRoute

@Composable
fun FirebasePhoneAuthScreen(
    onLoginSuccess: (prismaUserId: String) -> Unit,
) {
    AuthRoute(onLoginSuccess = onLoginSuccess)
}
