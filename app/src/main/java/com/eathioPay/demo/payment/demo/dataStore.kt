package com.eathioPay.demo.payment.demo

import android.content.Context
import com.eathioPay.demo.core.session.DataStoreSessionStore

private fun store(context: Context) = DataStoreSessionStore(context)

suspend fun saveAuthData(
    context: Context,
    prismaUserId: String,
    phone: String,
) {
    store(context).saveAuth(prismaUserId, phone)
}

suspend fun getPrismaUserId(context: Context): String? = store(context).getSnapshot().userId

suspend fun getSavedIdHash(context: Context): String? = store(context).getSnapshot().kycHash

suspend fun isKycDone(context: Context): Boolean = store(context).getSnapshot().isKycDone

suspend fun saveKycHash(context: Context, hash: String) {
    store(context).saveKycHash(hash)
}

suspend fun isSessionUnlocked(context: Context): Boolean = store(context).getSnapshot().isSessionUnlocked

suspend fun setSessionUnlocked(context: Context, value: Boolean) {
    store(context).setSessionUnlocked(value)
}

suspend fun setFaceVerified(context: Context, value: Boolean) {
    store(context).setFaceVerified(value)
}

suspend fun isFaceVerified(context: Context): Boolean = store(context).getSnapshot().isFaceVerified

suspend fun clearFaceVerified(context: Context) {
    store(context).setFaceVerified(false)
}

suspend fun getSavedPhone(context: Context): String? = store(context).getSnapshot().phone

suspend fun clearAuthData(context: Context) {
    store(context).clearAuth()
}

suspend fun getOrCreateUdid(context: Context): String = store(context).getOrCreateUdid()
