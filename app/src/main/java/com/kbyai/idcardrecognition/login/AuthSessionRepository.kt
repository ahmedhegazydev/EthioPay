package com.kbyai.idcardrecognition.login

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.kbyai.idcardrecognition.noble.data.NobleCloudStore
import com.kbyai.idcardrecognition.payment.demo.clearAuthData
import com.kbyai.idcardrecognition.payment.demo.getOrCreateUdid
import com.kbyai.idcardrecognition.payment.demo.getSavedAuthSnapshot
import com.kbyai.idcardrecognition.payment.demo.saveAuthData

interface AuthSessionGateway {
    suspend fun exchangeFirebaseAuth(firebaseUid: String, phone: String?): Result<String>
    suspend fun persistResolvedAuth(prismaUserId: String, firebaseUid: String)
    suspend fun completeSimulatedLogin(): Result<String>
    suspend fun getSavedAuthSnapshot(): SavedAuthSnapshot
    suspend fun clearAuthData()
}

class AuthSessionRepository(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val cloudStore: NobleCloudStore,
) : AuthSessionGateway {

    override suspend fun exchangeFirebaseAuth(firebaseUid: String, phone: String?): Result<String> {
        return runCatching {
            cloudStore.ensureUserProfile(
                firebaseUid = firebaseUid,
                phone = phone,
            )
        }.recoverCatching {
            firebaseUid
        }
    }

    override suspend fun persistResolvedAuth(prismaUserId: String, firebaseUid: String) {
        saveAuthData(
            context = context,
            prismaUserId = prismaUserId,
            firebaseUid = firebaseUid,
        )
    }

    override suspend fun completeSimulatedLogin(): Result<String> {
        val udid = getOrCreateUdid(context)
        persistResolvedAuth(prismaUserId = udid, firebaseUid = udid)
        return Result.success(udid)
    }

    override suspend fun getSavedAuthSnapshot(): SavedAuthSnapshot {
        return getSavedAuthSnapshot(context)
    }

    override suspend fun clearAuthData() {
        auth.signOut()
        clearAuthData(context)
    }
}
