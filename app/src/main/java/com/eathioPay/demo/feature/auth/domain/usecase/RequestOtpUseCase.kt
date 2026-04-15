package com.eathioPay.demo.feature.auth.domain.usecase

import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.core.common.AppResult.Failure
import com.eathioPay.demo.core.common.AppResult.Success
import com.eathioPay.demo.core.common.AppError
import com.eathioPay.demo.feature.auth.domain.model.OtpRequest
import com.eathioPay.demo.feature.auth.domain.repository.AuthRepository

class RequestOtpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(rawPhone: String): AppResult<OtpRequest> {
        val normalized = PhoneNormalizer.normalize(rawPhone)
            ?: return Failure(AppError.Validation("Enter a valid phone number."))
        return when (val result = repository.requestOtp(normalized)) {
            is Failure -> result
            is Success -> Success(result.value.copy(phone = normalized))
        }
    }
}
