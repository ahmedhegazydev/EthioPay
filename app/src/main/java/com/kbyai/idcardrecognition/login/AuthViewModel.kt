package com.kbyai.idcardrecognition.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kbyai.idcardrecognition.BuildConfig

class AuthViewModel(
    private val repo: PhoneAuthRepository = PhoneAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // ✅ timer job in VM
    private var resendJob: Job? = null
    private val RESEND_SECONDS = 120 // 2 minutes (غيّرها براحتك)

    fun onEvent(e: AuthEvent) {
        when (e) {
            is AuthEvent.PhoneChanged -> {
                _state.update {
                    it.copy(
                        phone = e.v,
                        message = null
                        // اختياري: لو عايز تمسح verificationId لما يغير الرقم:
                        // verificationId = null,
                        // otp = ""
                    )
                }
            }
            is AuthEvent.OtpChanged -> _state.update { it.copy(otp = e.v, message = null) }

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

        // ✅ prevent resend while timer running
        if (_state.value.resendSecondsLeft > 0) return

        // ✅ Simulation mode
        if (BuildConfig.SIMULATE_OTP) {
            startResendTimer()
            _state.update {
                it.copy(
                    isSendingOtp = false,
                    isVerifying = false,
                    verificationId = "SIMULATED",
                    canResend = true,
                    message = "تم إرسال الكود (Sim). استخدم 123456"
                )
            }
            return
        }

        val phoneE164 = if (phone.startsWith("+")) phone else "+2$phone"

        _state.update {
            it.copy(
                isSendingOtp = true,
                message = null
            )
        }

        viewModelScope.launch {
            repo.sendOtp(activity, phoneE164, resendToken).collect { r ->
                when (r) {
                    is PhoneAuthResult.CodeSent -> {
                        resendToken = r.token

                        // ✅ start timer AFTER CodeSent only
                        startResendTimer()

                        _state.update {
                            it.copy(
                                isSendingOtp = false,
                                verificationId = r.verificationId,
                                canResend = true,
                                message = "تم إرسال الكود"
                            )
                        }
                    }

                    is PhoneAuthResult.Verified -> {
                        // (نادراً بيتعمل Auto-Verify)
                        val res = repo.signInWithCredential(r.credential)
                        _state.update {
                            it.copy(
                                isSendingOtp = false,
                                isLoggedIn = res.isSuccess,
                                message = if (res.isSuccess) "تم تسجيل الدخول" else
                                    (res.exceptionOrNull()?.localizedMessage ?: "خطأ")
                            )
                        }
                    }

                    is PhoneAuthResult.Error -> {
                        _state.update { it.copy(isSendingOtp = false, message = r.msg) }
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

        // ✅ Simulation
        if (BuildConfig.SIMULATE_OTP) {
            val ok = st.otp.trim() == "123456"
            _state.update {
                it.copy(
                    isVerifying = false,
                    isLoggedIn = ok,
                    message = if (ok) "تم تسجيل الدخول (Sim)" else "الكود غلط (استخدم 123456)"
                )
            }
            return
        }

        _state.update { it.copy(isVerifying = true, message = null) }

        viewModelScope.launch {
            val cred = repo.credentialFromCode(vid, st.otp)
            val res = repo.signInWithCredential(cred)
            _state.update {
                it.copy(
                    isVerifying = false,
                    isLoggedIn = res.isSuccess,
                    message = if (res.isSuccess) "تم تسجيل الدخول"
                    else (res.exceptionOrNull()?.localizedMessage ?: "فشل التحقق")
                )
            }
        }
    }

    private fun startResendTimer() {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            for (t in RESEND_SECONDS downTo 1) {
                _state.update { it.copy(resendSecondsLeft = t) }
                delay(1000)
            }
            _state.update { it.copy(resendSecondsLeft = 0) }
        }
    }
}
