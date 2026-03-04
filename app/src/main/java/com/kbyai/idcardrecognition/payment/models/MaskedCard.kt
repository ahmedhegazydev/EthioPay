package com.kbyai.idcardrecognition.payment.models

data class MaskedCard(
    val brand: String? = null,
    val last4: String,
    val expiry: String? = null,
    val holder: String? = null
)
