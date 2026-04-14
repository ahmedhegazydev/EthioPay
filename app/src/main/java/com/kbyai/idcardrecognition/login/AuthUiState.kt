package com.kbyai.idcardrecognition.login

import android.app.Activity


data class AuthUiState(
    val phone: String = "",
    val otp: String = "",
    val verificationId: String? = null,
    val canResend: Boolean = false,
    val isSendingOtp: Boolean = false,
    val isVerifying: Boolean = false,
    val isCompletingLogin: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val prismaUserId: String? = null,
    val message: String? = null,
) {
    val isLoggedIn: Boolean
        get() = !prismaUserId.isNullOrBlank()
}

sealed class AuthEvent {
    data class PhoneChanged(val v: String) : AuthEvent()
    data class OtpChanged(val v: String) : AuthEvent()
    object BackToPhone : AuthEvent()

    data class SendOtp(val activity: Activity?) : AuthEvent()
    object VerifyOtp : AuthEvent()

    object ClearMessage : AuthEvent()
}
