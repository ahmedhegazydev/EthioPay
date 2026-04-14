package com.kbyai.idcardrecognition.login

import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.kbyai.idcardrecognition.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `successful otp verification persists prisma user id`() = runTest {
        val phoneGateway = FakePhoneAuthGateway()
        val sessionGateway = FakeAuthSessionGateway().apply {
            exchangeResult = Result.success("prisma-42")
        }
        val viewModel = AuthViewModel(phoneGateway, sessionGateway)

        viewModel.onEvent(AuthEvent.PhoneChanged("+251911111111"))
        viewModel.onEvent(AuthEvent.SendOtp(null))
        viewModel.onEvent(AuthEvent.OtpChanged("123456"))
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.VerifyOtp)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("verification-id", state.verificationId)
        assertEquals("prisma-42", state.prismaUserId)
        assertEquals("prisma-42", sessionGateway.persistedPrismaUserId)
        assertEquals("firebase-uid", sessionGateway.persistedFirebaseUid)
        assertFalse(sessionGateway.clearAuthDataCalled)
    }

    @Test
    fun `backend auth timeout clears partial session and surfaces message`() = runTest {
        val phoneGateway = FakePhoneAuthGateway()
        val sessionGateway = FakeAuthSessionGateway().apply {
            exchangeResult = Result.failure(
                IllegalStateException("Backend auth request timed out, please retry."),
            )
        }
        val viewModel = AuthViewModel(phoneGateway, sessionGateway)

        viewModel.onEvent(AuthEvent.PhoneChanged("+251911111111"))
        viewModel.onEvent(AuthEvent.SendOtp(null))
        viewModel.onEvent(AuthEvent.OtpChanged("123456"))
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.VerifyOtp)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.prismaUserId)
        assertEquals("Backend auth request timed out, please retry.", state.message)
        assertTrue(sessionGateway.clearAuthDataCalled)
        assertNull(sessionGateway.persistedPrismaUserId)
    }

    @Test
    fun `back to phone resets otp step state`() = runTest {
        val viewModel = AuthViewModel(FakePhoneAuthGateway(), FakeAuthSessionGateway())

        viewModel.onEvent(AuthEvent.PhoneChanged("+251911111111"))
        viewModel.onEvent(AuthEvent.SendOtp(null))
        advanceUntilIdle()

        assertEquals("verification-id", viewModel.state.value.verificationId)

        viewModel.onEvent(AuthEvent.BackToPhone)

        val state = viewModel.state.value
        assertNull(state.verificationId)
        assertEquals("", state.otp)
        assertEquals(0, state.resendSecondsLeft)
        assertFalse(state.isCompletingLogin)
    }

    private class FakePhoneAuthGateway : PhoneAuthGateway {
        private val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential("verification-id", "123456")

        override fun sendOtp(
            activity: android.app.Activity?,
            phoneE164: String,
            resendToken: PhoneAuthProvider.ForceResendingToken?,
        ): Flow<PhoneAuthResult> = flowOf(
            PhoneAuthResult.CodeSent("verification-id", null),
        )

        override suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<Unit> =
            Result.success(Unit)

        override fun credentialFromCode(
            verificationId: String,
            code: String,
        ): PhoneAuthCredential = credential

        override fun currentIdentity(): FirebaseIdentity = FirebaseIdentity(
            uid = "firebase-uid",
            phoneNumber = "+251911111111",
        )
    }

    private class FakeAuthSessionGateway : AuthSessionGateway {
        var exchangeResult: Result<String> = Result.success("prisma-1")
        var persistedPrismaUserId: String? = null
        var persistedFirebaseUid: String? = null
        var clearAuthDataCalled = false

        override suspend fun exchangeFirebaseAuth(firebaseUid: String, phone: String?): Result<String> =
            exchangeResult

        override suspend fun persistResolvedAuth(prismaUserId: String, firebaseUid: String) {
            persistedPrismaUserId = prismaUserId
            persistedFirebaseUid = firebaseUid
        }

        override suspend fun completeSimulatedLogin(): Result<String> = Result.success("sim-user")

        override suspend fun getSavedAuthSnapshot(): SavedAuthSnapshot = SavedAuthSnapshot(
            prismaUserId = persistedPrismaUserId,
            firebaseUid = persistedFirebaseUid,
        )

        override suspend fun clearAuthData() {
            clearAuthDataCalled = true
            persistedPrismaUserId = null
            persistedFirebaseUid = null
        }
    }
}
