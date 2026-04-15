package com.eathioPay.demo.feature.auth.data.repository

import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.core.common.AppResult.Failure
import com.eathioPay.demo.core.common.AppResult.Success
import com.eathioPay.demo.core.common.AppDispatchers
import com.eathioPay.demo.core.common.toAppError
import com.eathioPay.demo.core.session.SessionSnapshot
import com.eathioPay.demo.core.session.SessionStore
import com.eathioPay.demo.feature.auth.data.remote.AuthApiService
import com.eathioPay.demo.feature.auth.data.remote.OtpRequestBody
import com.eathioPay.demo.feature.auth.data.remote.VerifyOtpBody
import com.eathioPay.demo.feature.auth.domain.model.AuthSession
import com.eathioPay.demo.feature.auth.domain.model.OtpRequest
import com.eathioPay.demo.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val sessionStore: SessionStore,
    private val dispatchers: AppDispatchers,
) : AuthRepository {

    override suspend fun requestOtp(phone: String): AppResult<OtpRequest> = withContext(dispatchers.io) {
        runCatching {
            api.requestOtp(OtpRequestBody(phone))
        }.fold(
            onSuccess = {
                Success(
                    OtpRequest(
                        userId = it.userId,
                        phone = it.phone,
                        otpRequired = it.otpRequired,
                    )
                )
            },
            onFailure = { Failure(it.toAppError()) },
        )
    }

    override suspend fun verifyOtp(phone: String, otp: String): AppResult<AuthSession> = withContext(dispatchers.io) {
        runCatching {
            api.verifyOtp(VerifyOtpBody(phone = phone, otp = otp))
        }.fold(
            onSuccess = {
                sessionStore.saveAuth(it.userId, it.phone)
                Success(AuthSession(userId = it.userId, phone = it.phone))
            },
            onFailure = { Failure(it.toAppError()) },
        )
    }

    override fun observeSession(): Flow<SessionSnapshot> = sessionStore.session
}
