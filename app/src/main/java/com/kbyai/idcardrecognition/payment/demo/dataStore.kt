package com.kbyai.idcardrecognition.payment.demo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.booleanPreferencesKey
import java.util.UUID

val Context.dataStore by preferencesDataStore(name = "app_storage")

object StorageKeys {
    val PRISMA_USER_ID = stringPreferencesKey("prisma_user_id")
    val PHONE = stringPreferencesKey("phone")
    val API_KEY = stringPreferencesKey("api_key")
}

suspend fun saveAuthData(
    context: Context,
    prismaUserId: String,
    phone: String
) {
    context.dataStore.edit { prefs ->
        prefs[StorageKeys.PRISMA_USER_ID] = prismaUserId
        prefs[StorageKeys.PHONE] = phone
    }
}


suspend fun getPrismaUserId(context: Context): String? {
    return context.dataStore.data
        .map { it[StorageKeys.PRISMA_USER_ID] }
        .first()
}



object KycKeys {
    val ID_HASH = stringPreferencesKey("kyc_id_hash")
    val KYC_DONE = booleanPreferencesKey("kyc_done")
}


suspend fun getSavedIdHash(context: Context): String? {
    return context.dataStore.data.map { it[KycKeys.ID_HASH] }.first()
}

suspend fun isKycDone(context: Context): Boolean {
    return context.dataStore.data.map { it[KycKeys.KYC_DONE] ?: false }.first()
}

suspend fun saveKycHash(context: Context, hash: String) {
    context.dataStore.edit { prefs ->
        prefs[KycKeys.ID_HASH] = hash
        prefs[KycKeys.KYC_DONE] = true
    }
}

private val KEY_SESSION_UNLOCKED = booleanPreferencesKey("session_unlocked")

suspend fun isSessionUnlocked(context: Context): Boolean {
    return context.dataStore.data.map { it[KEY_SESSION_UNLOCKED] ?: false }.first()
}

suspend fun setSessionUnlocked(context: Context, value: Boolean) {
    context.dataStore.edit { it[KEY_SESSION_UNLOCKED] = value }
}

object FaceKeys {
    val FACE_OK = booleanPreferencesKey("face_ok")
}

suspend fun setFaceVerified(context: Context, value: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[FaceKeys.FACE_OK] = value
    }
}

suspend fun isFaceVerified(context: Context): Boolean {
    return context.dataStore.data
        .map { it[FaceKeys.FACE_OK] ?: false }
        .first()
}

suspend fun clearFaceVerified(context: Context) {
    setFaceVerified(context, false)
}

private val KEY_UDID = stringPreferencesKey("udid")

suspend fun getSavedPhone(context: Context): String? {
    return context.dataStore.data
        .map { it[StorageKeys.PHONE] }
        .first()
}

suspend fun clearAuthData(context: Context) {
    context.dataStore.edit { prefs ->
        prefs.remove(StorageKeys.PRISMA_USER_ID)
        prefs.remove(StorageKeys.PHONE)
        prefs.remove(KEY_SESSION_UNLOCKED)
        prefs.remove(FaceKeys.FACE_OK)
        prefs.remove(KycKeys.ID_HASH)
        prefs.remove(KycKeys.KYC_DONE)
    }
}

suspend fun getOrCreateUdid(context: Context): String {
    val existing = context.dataStore.data
        .map { it[KEY_UDID] }
        .first()

    if (!existing.isNullOrBlank()) return existing

    val newId = UUID.randomUUID().toString()
    context.dataStore.edit { prefs ->
        prefs[KEY_UDID] = newId
    }
    return newId
}
