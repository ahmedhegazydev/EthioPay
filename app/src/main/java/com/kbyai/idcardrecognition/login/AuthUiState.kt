package com.kbyai.idcardrecognition.login

import android.app.Activity


data class AuthUiState(
    val phone: String = "+201156749640",
    val otp: String = "123456",
    val verificationId: String? = null,
    val canResend: Boolean = false,
    val isSendingOtp: Boolean = false,
    val isVerifying: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val isLoggedIn: Boolean = false,
    val message: String? = null,
)

sealed class AuthEvent {
    data class PhoneChanged(val v: String) : AuthEvent()
    data class OtpChanged(val v: String) : AuthEvent()

    data class SendOtp(val activity: Activity) : AuthEvent()
    object VerifyOtp : AuthEvent()

    object ClearMessage : AuthEvent()
}