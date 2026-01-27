package com.example.ethiopay.payment.models

//data class ScannedCardUi(
//    val brand: String?,
//    val last4: String,
//    val holder: String?,
//    val exp: String?
//)

data class ScannedCardUi(
    val number: String,
    val expMonth: Int?,
    val expYear: Int?,
    val holder: String? = null
)
