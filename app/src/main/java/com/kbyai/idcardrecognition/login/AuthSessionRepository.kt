package com.kbyai.idcardrecognition.login

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.kbyai.idcardrecognition.payment.demo.clearAuthData
import com.kbyai.idcardrecognition.payment.demo.getOrCreateUdid
import com.kbyai.idcardrecognition.payment.demo.getSavedAuthSnapshot
import com.kbyai.idcardrecognition.payment.demo.saveAuthData
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

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
    private val api: AuthApiService,
) : AuthSessionGateway {

    override suspend fun exchangeFirebaseAuth(firebaseUid: String, phone: String?): Result<String> {
        return runCatching {
            api.exchangeFirebaseAuth(
                FirebaseAuthExchangeRequest(
                    firebaseUid = firebaseUid,
                    phone = phone,
                )
            ).userId
        }.mapFailure(::mapExchangeError)
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

    private fun mapExchangeError(throwable: Throwable): Throwable = when (throwable) {
        is SocketTimeoutException -> IllegalStateException(
            "Backend auth request timed out, please retry.",
            throwable,
        )

        is HttpException -> IllegalStateException(
            throwable.response()?.errorBody()?.string()
                ?.takeIf { it.isNotBlank() }
                ?: "Backend auth request failed.",
            throwable,
        )

        is IOException -> IllegalStateException(
            "Couldn't reach the backend. Check your connection and retry.",
            throwable,
        )

        else -> throwable
    }
}

private inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
    val failure = exceptionOrNull() ?: return this
    return Result.failure(transform(failure))
}
