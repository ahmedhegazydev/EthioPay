package com.example.ethiopay.payment.models

data class ScannedCardUi(
    val brand: String?,
    val last4: String,
    val holder: String?,
    val exp: String?
)
