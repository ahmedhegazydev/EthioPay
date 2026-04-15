package com.eathioPay.demo.feature.auth.presentation

import com.eathioPay.demo.core.common.message
import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.feature.auth.domain.model.AuthSession
import com.eathioPay.demo.feature.auth.domain.model.OtpRequest

object AuthReducer {
    fun phoneChanged(state: AuthUiState, value: String): AuthUiState = state.copy(
        phone = value,
        message = null,
    )

    fun otpChanged(state: AuthUiState, value: String): AuthUiState = state.copy(
        otp = value.filter(Char::isDigit).take(6),
        message = null,
    )

    fun sendingOtp(state: AuthUiState): AuthUiState = state.copy(
        isSendingOtp = true,
        message = null,
    )

    fun otpRequested(state: AuthUiState, request: OtpRequest): AuthUiState = state.copy(
        phone = request.phone,
        normalizedPhone = request.phone,
        otpRequested = true,
        isSendingOtp = false,
        message = "OTP sent. Use 123456 for the demo.",
    )

    fun otpRequestFailed(state: AuthUiState, result: AppResult.Failure): AuthUiState = state.copy(
        isSendingOtp = false,
        message = result.error.message(),
    )

    fun verifying(state: AuthUiState): AuthUiState = state.copy(
        isVerifying = true,
        message = null,
    )

    fun verified(state: AuthUiState, session: AuthSession): AuthUiState = state.copy(
        isVerifying = false,
        isLoggedIn = true,
        userId = session.userId,
        phone = session.phone,
        normalizedPhone = session.phone,
        message = "Logged in successfully.",
    )

    fun verifyFailed(state: AuthUiState, result: AppResult.Failure): AuthUiState = state.copy(
        isVerifying = false,
        isLoggedIn = false,
        userId = null,
        message = result.error.message(),
    )

    fun tick(state: AuthUiState, secondsLeft: Int): AuthUiState = state.copy(resendSecondsLeft = secondsLeft)

    fun clearMessage(state: AuthUiState): AuthUiState = state.copy(message = null)
}
