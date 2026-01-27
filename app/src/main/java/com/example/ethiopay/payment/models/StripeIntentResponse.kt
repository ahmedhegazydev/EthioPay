package com.example.ethiopay.payment.models

data class StripeIntentResponse(
    val clientSecret: String,
    val customerId: String,
    val ephemeralKey: String
)
