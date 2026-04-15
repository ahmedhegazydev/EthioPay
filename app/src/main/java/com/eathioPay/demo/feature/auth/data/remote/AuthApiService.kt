package com.eathioPay.demo.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/request-otp")
    suspend fun requestOtp(@Body body: OtpRequestBody): OtpResponseDto

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpBody): AuthSessionDto
}

data class OtpRequestBody(
    val phone: String,
)

data class VerifyOtpBody(
    val phone: String,
    val otp: String,
)

data class OtpResponseDto(
    val userId: String,
    val phone: String,
    val otpRequired: Boolean,
)

data class AuthSessionDto(
    val userId: String,
    val phone: String,
)
