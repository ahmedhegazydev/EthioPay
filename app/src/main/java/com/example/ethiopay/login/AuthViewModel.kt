package com.example.ethiopay.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
  private val repo: PhoneAuthRepository = PhoneAuthRepository()
) : ViewModel() {

  private val _state = MutableStateFlow(AuthUiState())
  val state: StateFlow<AuthUiState> = _state

  private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

  fun onEvent(e: AuthEvent) {
    when (e) {
      is AuthEvent.PhoneChanged -> _state.update { it.copy(phone = e.v) }
      is AuthEvent.OtpChanged -> _state.update { it.copy(otp = e.v) }

      is AuthEvent.SendOtp -> sendOtp(e.activity)
      AuthEvent.VerifyOtp -> verifyOtp()

      AuthEvent.ClearMessage -> _state.update { it.copy(message = null) }
    }
  }

  private fun sendOtp(activity: Activity) {
    val phone = _state.value.phone.trim()
    if (phone.isBlank()) {
      _state.update { it.copy(message = "اكتب رقم الموبايل") }
      return
    }

    // لازم يكون E.164 مثال مصر: +2010xxxxxxx
    val phoneE164 = if (phone.startsWith("+")) phone else "+2$phone"

    _state.update { it.copy(isLoading = true, message = null) }

    viewModelScope.launch {
      repo.sendOtp(activity, phoneE164, resendToken).collect { r ->
        when (r) {
          is PhoneAuthResult.CodeSent -> {
            resendToken = r.token
            _state.update {
              it.copy(
                isLoading = false,
                verificationId = r.verificationId,
                canResend = true,
                message = "تم إرسال الكود"
              )
            }
          }
          is PhoneAuthResult.Verified -> {
            val res = repo.signInWithCredential(r.credential)
            _state.update {
              it.copy(
                isLoading = false,
                isLoggedIn = res.isSuccess,
                message = if (res.isSuccess) "تم تسجيل الدخول" else (res.exceptionOrNull()?.localizedMessage ?: "خطأ")
              )
            }
          }
          is PhoneAuthResult.Error -> {
            _state.update { it.copy(isLoading = false, message = r.msg) }
          }
        }
      }
    }
  }

  private fun verifyOtp() {
    val st = _state.value
    val vid = st.verificationId
    if (vid.isNullOrBlank()) {
      _state.update { it.copy(message = "ابعت OTP الأول") }
      return
    }
    if (st.otp.length < 6) {
      _state.update { it.copy(message = "اكتب الكود 6 أرقام") }
      return
    }

    _state.update { it.copy(isLoading = true, message = null) }

    viewModelScope.launch {
      val cred = repo.credentialFromCode(vid, st.otp)
      val res = repo.signInWithCredential(cred)
      _state.update {
        it.copy(
          isLoading = false,
          isLoggedIn = res.isSuccess,
          message = if (res.isSuccess) "تم تسجيل الدخول" else (res.exceptionOrNull()?.localizedMessage ?: "فشل التحقق")
        )
      }
    }
  }
}