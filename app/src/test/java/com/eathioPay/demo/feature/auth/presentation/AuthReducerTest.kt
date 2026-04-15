package com.eathioPay.demo.feature.auth.presentation

import com.eathioPay.demo.core.common.AppError
import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.feature.auth.domain.model.AuthSession
import com.eathioPay.demo.feature.auth.domain.model.OtpRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthReducerTest {

    @Test
    fun `otp requested updates state`() {
        val result = AuthReducer.otpRequested(
            state = AuthUiState(),
            request = OtpRequest(userId = "u1", phone = "+201156749640", otpRequired = true),
        )

        assertTrue(result.otpRequested)
        assertEquals("+201156749640", result.normalizedPhone)
        assertFalse(result.isSendingOtp)
    }

    @Test
    fun `verify failed resets login state`() {
        val result = AuthReducer.verifyFailed(
            state = AuthUiState(isVerifying = true, isLoggedIn = true, userId = "u1"),
            result = AppResult.Failure(AppError.Validation("Wrong OTP")),
        )

        assertFalse(result.isVerifying)
        assertFalse(result.isLoggedIn)
        assertEquals(null, result.userId)
        assertEquals("Wrong OTP", result.message)
    }

    @Test
    fun `verified stores session details`() {
        val result = AuthReducer.verified(
            state = AuthUiState(isVerifying = true),
            session = AuthSession(userId = "user-1", phone = "+201156749640"),
        )

        assertTrue(result.isLoggedIn)
        assertEquals("user-1", result.userId)
        assertEquals("+201156749640", result.phone)
    }
}
