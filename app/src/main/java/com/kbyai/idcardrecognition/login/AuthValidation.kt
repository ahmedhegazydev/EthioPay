package com.kbyai.idcardrecognition.login

import com.kbyai.idcardrecognition.payment.idcardrecognition.normalizeDigits

internal object AuthValidation {

    fun validatePhoneForOtp(phone: String): String? {
        val normalizedDigits = normalizeDigits(phone).filter(Char::isDigit)
        return when {
            normalizedDigits.isBlank() -> "اكتب رقم الموبايل"
            normalizedDigits.length < 9 -> "اكتب رقم صحيح بصيغة كاملة"
            else -> null
        }
    }

    fun sanitizePhoneInput(phone: String): String = normalizeDigits(phone).filter {
        it.isDigit() || it == '+' || it == ' ' || it == '-'
    }

    fun normalizeOtp(otp: String): String = normalizeDigits(otp)
        .filter(Char::isDigit)
        .take(6)

    fun normalizeNumericInput(raw: String): String = normalizeDigits(raw).filter(Char::isDigit)

    fun normalizePhone(phone: String): String {
        val trimmed = sanitizePhoneInput(phone)
            .replace(" ", "")
            .replace("-", "")
            .trim()

        if (trimmed.isBlank()) return trimmed
        if (trimmed.startsWith("00")) return "+${trimmed.drop(2)}"
        if (trimmed.startsWith("+")) return trimmed

        return when {
            trimmed.startsWith("09") && trimmed.length == 10 -> "+251${trimmed.drop(1)}"
            trimmed.startsWith("9") && trimmed.length == 9 -> "+251$trimmed"
            trimmed.startsWith("0") && trimmed.length == 11 -> "+20${trimmed.drop(1)}"
            else -> "+$trimmed"
        }
    }

    fun validateOtpSubmission(state: AuthUiState): String? = when {
        state.verificationId.isNullOrBlank() -> "ابعت OTP الأول"
        normalizeOtp(state.otp).length < 6 -> "اكتب الكود 6 أرقام"
        else -> null
    }
}
