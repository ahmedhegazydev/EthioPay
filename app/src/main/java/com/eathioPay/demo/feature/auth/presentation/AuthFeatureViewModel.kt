package com.eathioPay.demo.feature.auth.presentation

import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.core.mvi.MviViewModel
import com.eathioPay.demo.feature.auth.domain.usecase.RequestOtpUseCase
import com.eathioPay.demo.feature.auth.domain.usecase.VerifyOtpUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class AuthFeatureViewModel(
    private val requestOtpUseCase: RequestOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
) : MviViewModel<AuthIntent, AuthUiState, AuthEffect>(AuthUiState()) {

    private var resendJob: Job? = null
    private val resendSeconds = 120

    override fun onIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.ClearMessage -> setState(AuthReducer::clearMessage)
            is AuthIntent.PhoneChanged -> setState { AuthReducer.phoneChanged(it, intent.value) }
            is AuthIntent.OtpChanged -> setState { AuthReducer.otpChanged(it, intent.value) }
            AuthIntent.SendOtp -> requestOtp()
            AuthIntent.VerifyOtp -> verifyOtp()
        }
    }

    private fun requestOtp() {
        val current = state.value
        if (current.isSendingOtp || current.resendSecondsLeft > 0) return
        setState(AuthReducer::sendingOtp)

        viewModelScope.launch {
            when (val result = requestOtpUseCase(state.value.phone)) {
                is AppResult.Failure -> setState { AuthReducer.otpRequestFailed(it, result) }
                is AppResult.Success -> {
                    startResendTimer()
                    setState { AuthReducer.otpRequested(it, result.value) }
                }
            }
        }
    }

    private fun verifyOtp() {
        val current = state.value
        if (current.isVerifying) return
        setState(AuthReducer::verifying)

        viewModelScope.launch {
            when (val result = verifyOtpUseCase(current.normalizedPhone.orEmpty(), current.otp)) {
                is AppResult.Failure -> setState { AuthReducer.verifyFailed(it, result) }
                is AppResult.Success -> {
                    setState { AuthReducer.verified(it, result.value) }
                    sendEffect { AuthEffect.NavigateNext(result.value.userId) }
                }
            }
        }
    }

    private fun startResendTimer() {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            for (secondsLeft in resendSeconds downTo 1) {
                setState { AuthReducer.tick(it, secondsLeft) }
                delay(1000)
            }
            setState { AuthReducer.tick(it, 0) }
        }
    }
}
