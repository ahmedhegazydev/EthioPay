package com.example.ethiopay.payment.stripe

fun detectBrand(cardNumber: String): String {
    return when {
        cardNumber.startsWith("4") -> "Visa"
        cardNumber.startsWith("5") -> "Mastercard"
        cardNumber.startsWith("34") || cardNumber.startsWith("37") -> "Amex"
        cardNumber.startsWith("6") -> "Discover"
        else -> "Card"
    }
}
