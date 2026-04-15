package com.kbyai.idcardrecognition.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbyai.idcardrecognition.payment.demo.DemoApi
import com.kbyai.idcardrecognition.payment.idcardrecognition.normalizeDigits
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    private var resendJob: Job? = null
    private val resendSeconds = 120

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.PhoneChanged -> {
                _state.update {
                    it.copy(
                        phone = event.v,
                        message = null,
                    )
                }
            }

            is AuthEvent.OtpChanged -> {
                _state.update {
                    it.copy(
                        otp = normalizeDigits(event.v).filter(Char::isDigit).take(6),
                        message = null,
                    )
                }
            }

            AuthEvent.SendOtp -> requestOtp()
            AuthEvent.VerifyOtp -> verifyOtp()
            AuthEvent.ClearMessage -> _state.update { it.copy(message = null) }
        }
    }

    private fun requestOtp() {
        val normalizedPhone = normalizePhoneInput(_state.value.phone)
        if (normalizedPhone == null) {
            _state.update { it.copy(message = "Enter a valid phone number.") }
            return
        }

        if (_state.value.isSendingOtp || _state.value.resendSecondsLeft > 0) return

        _state.update {
            it.copy(
                isSendingOtp = true,
                message = null,
                normalizedPhone = normalizedPhone,
            )
        }

        viewModelScope.launch {
            runCatching { DemoApi.requestOtp(normalizedPhone) }
                .onSuccess { session ->
                    startResendTimer()
                    _state.update {
                        it.copy(
                            phone = session.phone,
                            normalizedPhone = session.phone,
                            otpRequested = true,
                            canResend = true,
                            isSendingOtp = false,
                            message = "OTP sent. Use 123456 for the demo.",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSendingOtp = false,
                            message = error.localizedMessage ?: "Unable to request OTP.",
                        )
                    }
                }
        }
    }

    private fun verifyOtp() {
        val state = _state.value
        val normalizedPhone = state.normalizedPhone ?: normalizePhoneInput(state.phone)
        if (!state.otpRequested || normalizedPhone == null) {
            _state.update { it.copy(message = "Request OTP first.") }
            return
        }
        if (state.otp.length != 6) {
            _state.update { it.copy(message = "Enter the 6-digit code.") }
            return
        }
        if (state.isVerifying) return

        _state.update { it.copy(isVerifying = true, message = null) }

        viewModelScope.launch {
            runCatching { DemoApi.verifyOtp(normalizedPhone, state.otp) }
                .onSuccess { authSession ->
                    _state.update {
                        it.copy(
                            isVerifying = false,
                            isLoggedIn = true,
                            userId = authSession.userId,
                            phone = authSession.phone,
                            normalizedPhone = authSession.phone,
                            message = "Logged in successfully.",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isVerifying = false,
                            isLoggedIn = false,
                            userId = null,
                            message = error.localizedMessage ?: "OTP verification failed.",
                        )
                    }
                }
        }
    }

    private fun startResendTimer() {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            for (secondsLeft in resendSeconds downTo 1) {
                _state.update { it.copy(resendSecondsLeft = secondsLeft) }
                delay(1000)
            }
            _state.update { it.copy(resendSecondsLeft = 0) }
        }
    }

    private fun normalizePhoneInput(raw: String): String? {
        val input = normalizeDigits(raw).trim()
        if (input.isBlank()) return null

        val hasPlus = input.startsWith("+")
        val digits = input.filter(Char::isDigit)
        if (digits.isBlank()) return null

        return when {
            hasPlus -> "+$digits"
            digits.startsWith("00") -> "+${digits.drop(2)}"
            digits.startsWith("201") && digits.length == 12 -> "+$digits"
            digits.startsWith("251") && digits.length == 12 -> "+$digits"
            digits.startsWith("01") && digits.length == 11 -> "+2$digits"
            digits.startsWith("1") && digits.length == 10 -> "+20$digits"
            digits.startsWith("09") && digits.length == 10 -> "+251${digits.drop(1)}"
            digits.startsWith("9") && digits.length == 9 -> "+251$digits"
            else -> "+$digits"
        }
    }
}
