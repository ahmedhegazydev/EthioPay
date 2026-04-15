package com.eathioPay.demo.feature.auth.domain.repository

import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.core.session.SessionSnapshot
import com.eathioPay.demo.feature.auth.domain.model.AuthSession
import com.eathioPay.demo.feature.auth.domain.model.OtpRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun requestOtp(phone: String): AppResult<OtpRequest>
    suspend fun verifyOtp(phone: String, otp: String): AppResult<AuthSession>
    fun observeSession(): Flow<SessionSnapshot>
}
