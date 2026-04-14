package com.kbyai.idcardrecognition.login

internal object AuthValidation {

    fun validatePhoneForOtp(phone: String): String? = if (phone.isBlank()) {
        "اكتب رقم الموبايل"
    } else {
        null
    }

    fun normalizePhone(phone: String): String {
        val trimmed = phone.trim()
        return if (trimmed.startsWith("+")) trimmed else "+2$trimmed"
    }

    fun validateOtpSubmission(state: AuthUiState): String? = when {
        state.verificationId.isNullOrBlank() -> "ابعت OTP الأول"
        state.otp.length < 6 -> "اكتب الكود 6 أرقام"
        else -> null
    }
}
