package com.kbyai.idcardrecognition.login

data class AuthUiState(
    val phone: String = "+201156749640",
    val normalizedPhone: String? = null,
    val otp: String = "123456",
    val otpRequested: Boolean = false,
    val canResend: Boolean = false,
    val isSendingOtp: Boolean = false,
    val isVerifying: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val message: String? = null,
)

sealed class AuthEvent {
    data class PhoneChanged(val v: String) : AuthEvent()
    data class OtpChanged(val v: String) : AuthEvent()

    object SendOtp : AuthEvent()
    object VerifyOtp : AuthEvent()

    object ClearMessage : AuthEvent()
}
