package com.eathioPay.demo.core.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.appDataStore by preferencesDataStore(name = "app_storage")

class DataStoreSessionStore(
    private val context: Context,
) : SessionStore {

    override val session: Flow<SessionSnapshot> = context.appDataStore.data.map { prefs ->
        prefs.toSnapshot()
    }

    override suspend fun saveAuth(userId: String, phone: String) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.PRISMA_USER_ID] = userId
            prefs[Keys.PHONE] = phone
        }
    }

    override suspend fun setSessionUnlocked(unlocked: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.SESSION_UNLOCKED] = unlocked
        }
    }

    override suspend fun setFaceVerified(verified: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.FACE_OK] = verified
        }
    }

    override suspend fun saveKycHash(hash: String) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.KYC_ID_HASH] = hash
            prefs[Keys.KYC_DONE] = true
        }
    }

    override suspend fun clearAuth() {
        context.appDataStore.edit { prefs ->
            prefs.remove(Keys.PRISMA_USER_ID)
            prefs.remove(Keys.PHONE)
            prefs.remove(Keys.SESSION_UNLOCKED)
            prefs.remove(Keys.FACE_OK)
            prefs.remove(Keys.KYC_DONE)
            prefs.remove(Keys.KYC_ID_HASH)
        }
    }

    override suspend fun getSnapshot(): SessionSnapshot = session.first()

    override suspend fun getOrCreateUdid(): String {
        val existing = context.appDataStore.data.map { it[Keys.UDID] }.first()
        if (!existing.isNullOrBlank()) return existing
        val newId = UUID.randomUUID().toString()
        context.appDataStore.edit { prefs ->
            prefs[Keys.UDID] = newId
        }
        return newId
    }

    private fun Preferences.toSnapshot(): SessionSnapshot = SessionSnapshot(
        userId = this[Keys.PRISMA_USER_ID],
        phone = this[Keys.PHONE],
        isSessionUnlocked = this[Keys.SESSION_UNLOCKED] ?: false,
        isFaceVerified = this[Keys.FACE_OK] ?: false,
        isKycDone = this[Keys.KYC_DONE] ?: false,
        kycHash = this[Keys.KYC_ID_HASH],
    )

    private object Keys {
        val PRISMA_USER_ID = stringPreferencesKey("prisma_user_id")
        val PHONE = stringPreferencesKey("phone")
        val SESSION_UNLOCKED = booleanPreferencesKey("session_unlocked")
        val FACE_OK = booleanPreferencesKey("face_ok")
        val KYC_DONE = booleanPreferencesKey("kyc_done")
        val KYC_ID_HASH = stringPreferencesKey("kyc_id_hash")
        val UDID = stringPreferencesKey("udid")
    }
}
