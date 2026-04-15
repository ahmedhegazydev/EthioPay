package com.eathioPay.demo.feature.auth.usecase

import com.eathioPay.demo.core.common.AppResult
import com.eathioPay.demo.feature.auth.domain.model.AuthSession
import com.eathioPay.demo.feature.auth.domain.model.OtpRequest
import com.eathioPay.demo.feature.auth.domain.repository.AuthRepository
import com.eathioPay.demo.feature.auth.domain.usecase.RequestOtpUseCase
import com.eathioPay.demo.feature.auth.domain.usecase.VerifyOtpUseCase
import com.eathioPay.demo.feature.auth.presentation.AuthEffect
import com.eathioPay.demo.feature.auth.presentation.AuthFeatureViewModel
import com.eathioPay.demo.feature.auth.presentation.AuthIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthFeatureViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `verify otp emits navigate effect`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthFeatureViewModel(
            requestOtpUseCase = RequestOtpUseCase(repository),
            verifyOtpUseCase = VerifyOtpUseCase(repository),
        )
        val effects = mutableListOf<AuthEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(AuthIntent.SendOtp)
        advanceUntilIdle()
        viewModel.onIntent(AuthIntent.VerifyOtp)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isLoggedIn)
        assertEquals(AuthEffect.NavigateNext("user-1"), effects.single())
        job.cancel()
    }
}

private class FakeAuthRepository : AuthRepository {
    override suspend fun requestOtp(phone: String): AppResult<OtpRequest> =
        AppResult.Success(OtpRequest(userId = "user-1", phone = phone, otpRequired = true))

    override suspend fun verifyOtp(phone: String, otp: String): AppResult<AuthSession> =
        AppResult.Success(AuthSession(userId = "user-1", phone = phone))

    override fun observeSession(): Flow<com.eathioPay.demo.core.session.SessionSnapshot> = emptyFlow()
}
