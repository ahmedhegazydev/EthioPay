package com.eathioPay.demo.feature.auth.domain.usecase

import com.eathioPay.demo.core.common.AppError
import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.core.common.AppResult.Failure
import com.eathioPay.demo.feature.auth.domain.model.AuthSession
import com.eathioPay.demo.feature.auth.domain.repository.AuthRepository

class VerifyOtpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(phone: String, otp: String): AppResult<AuthSession> {
        if (phone.isBlank()) return Failure(AppError.Validation("Request OTP first."))
        val normalizedOtp = otp.filter(Char::isDigit)
        if (normalizedOtp.length != 6) {
            return Failure(AppError.Validation("Enter the 6-digit code."))
        }
        return repository.verifyOtp(phone = phone, otp = normalizedOtp)
    }
}
