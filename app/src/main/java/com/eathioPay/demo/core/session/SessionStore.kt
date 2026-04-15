package com.eathioPay.demo.core.session

import kotlinx.coroutines.flow.Flow

data class SessionSnapshot(
    val userId: String? = null,
    val phone: String? = null,
    val isSessionUnlocked: Boolean = false,
    val isFaceVerified: Boolean = false,
    val isKycDone: Boolean = false,
    val kycHash: String? = null,
)

interface SessionStore {
    val session: Flow<SessionSnapshot>

    suspend fun saveAuth(userId: String, phone: String)
    suspend fun setSessionUnlocked(unlocked: Boolean)
    suspend fun setFaceVerified(verified: Boolean)
    suspend fun saveKycHash(hash: String)
    suspend fun clearAuth()
    suspend fun getSnapshot(): SessionSnapshot
    suspend fun getOrCreateUdid(): String
}
