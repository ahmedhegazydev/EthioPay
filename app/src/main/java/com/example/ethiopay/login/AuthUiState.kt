package com.example.ethiopay.login

import android.app.Activity

data class AuthUiState(
  val phone: String = "",
  val otp: String = "",
  val isLoading: Boolean = false,
  val verificationId: String? = null,
  val canResend: Boolean = false,
  val message: String? = null,
  val isLoggedIn: Boolean = false
)

sealed interface AuthEvent {
  data class PhoneChanged(val v: String) : AuthEvent
  data class OtpChanged(val v: String) : AuthEvent
  data class SendOtp(val activity: Activity) : AuthEvent
  object VerifyOtp : AuthEvent
  object ClearMessage : AuthEvent
}
