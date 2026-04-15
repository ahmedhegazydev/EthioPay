package com.eathioPay.demo.feature.auth.domain.model

data class AuthSession(
    val userId: String,
    val phone: String,
)

data class OtpRequest(
    val userId: String,
    val phone: String,
    val otpRequired: Boolean,
)
