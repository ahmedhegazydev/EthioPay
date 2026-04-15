package com.eathioPay.demo.feature.auth.domain.usecase

import com.eathioPay.demo.payment.idcardrecognition.normalizeDigits

object PhoneNormalizer {
    fun normalize(raw: String): String? {
        val input = normalizeDigits(raw).trim()
        if (input.isBlank()) return null

        val hasPlus = input.startsWith("+")
        val digits = input.filter(Char::isDigit)
        if (digits.isBlank()) return null

        return when {
            hasPlus -> "+$digits"
            digits.startsWith("00") -> "+${digits.drop(2)}"
            digits.startsWith("201") && digits.length == 12 -> "+$digits"
            digits.startsWith("251") && digits.length == 12 -> "+$digits"
            digits.startsWith("01") && digits.length == 11 -> "+2$digits"
            digits.startsWith("1") && digits.length == 10 -> "+20$digits"
            digits.startsWith("09") && digits.length == 10 -> "+251${digits.drop(1)}"
            digits.startsWith("9") && digits.length == 9 -> "+251$digits"
            else -> "+$digits"
        }
    }
}
