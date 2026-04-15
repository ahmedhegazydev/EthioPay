package com.eathioPay.demo.feature.auth.domain.usecase

import com.eathioPay.demo.core.session.SessionSnapshot
import com.eathioPay.demo.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<SessionSnapshot> = repository.observeSession()
}
