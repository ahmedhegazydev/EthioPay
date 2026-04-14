package com.kbyai.idcardrecognition.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {

    @Test
    fun `validate phone for otp rejects blank values`() {
        assertEquals("اكتب رقم الموبايل", AuthValidation.validatePhoneForOtp("   "))
    }

    @Test
    fun `normalize phone supports local egypt and ethiopia numbers`() {
        assertEquals("+201001234567", AuthValidation.normalizePhone("01001234567"))
        assertEquals("+251911111111", AuthValidation.normalizePhone("0911111111"))
        assertEquals("+251911111111", AuthValidation.normalizePhone("+251911111111"))
    }

    @Test
    fun `validate otp submission requires verification id and full otp`() {
        val missingVerification = AuthUiState(otp = "123456", verificationId = null)
        val shortOtp = AuthUiState(otp = "123", verificationId = "verification-id")
        val valid = AuthUiState(otp = "123456", verificationId = "verification-id")

        assertEquals("ابعت OTP الأول", AuthValidation.validateOtpSubmission(missingVerification))
        assertEquals("اكتب الكود 6 أرقام", AuthValidation.validateOtpSubmission(shortOtp))
        assertNull(AuthValidation.validateOtpSubmission(valid))
    }

    @Test
    fun `normalizers convert arabic digits to ascii`() {
        assertEquals("+251911111111", AuthValidation.normalizePhone("٠٩١١١١١١١١"))
        assertEquals("123456", AuthValidation.normalizeOtp("١٢٣٤٥٦"))
        assertEquals("2500", AuthValidation.normalizeNumericInput("٢٥٠٠"))
    }
}
