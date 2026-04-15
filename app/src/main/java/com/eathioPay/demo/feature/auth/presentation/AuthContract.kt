package com.eathioPay.demo.feature.auth.presentation

import androidx.compose.runtime.Immutable

sealed interface AuthIntent {
    data class PhoneChanged(val value: String) : AuthIntent
    data class OtpChanged(val value: String) : AuthIntent
    data object SendOtp : AuthIntent
    data object VerifyOtp : AuthIntent
    data object ClearMessage : AuthIntent
}

sealed interface AuthEffect {
    data class NavigateNext(val userId: String) : AuthEffect
}

@Immutable
data class AuthUiState(
    val phone: String = "+201156749640",
    val normalizedPhone: String? = null,
    val otp: String = "123456",
    val otpRequested: Boolean = false,
    val isSendingOtp: Boolean = false,
    val isVerifying: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val message: String? = null,
) {
    val canSubmitOtp: Boolean get() = !isVerifying && otp.length == 6
    val canResend: Boolean get() = otpRequested && resendSecondsLeft == 0 && !isSendingOtp
}
