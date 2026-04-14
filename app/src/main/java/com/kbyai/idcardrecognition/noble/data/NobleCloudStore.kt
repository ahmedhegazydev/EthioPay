package com.kbyai.idcardrecognition.noble.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.noble.domain.NobleWallet
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

interface NobleCloudStore {
    suspend fun ensureUserProfile(firebaseUid: String, phone: String?): String
    suspend fun loadHome(userId: String): NobleHomeSnapshot
    suspend fun transferMoney(
        fromUserId: String,
        recipient: String,
        amount: Int,
        note: String?,
    ): NobleHomeSnapshot

    suspend fun topUpWallet(userId: String, amount: Int): NobleWallet
    suspend fun markKycSubmitted(
        userId: String,
        idHash: String,
        documentNumber: String?,
    )
}

class FirestoreNobleCloudStore(
    private val firestore: FirebaseFirestore,
) : NobleCloudStore {

    override suspend fun ensureUserProfile(firebaseUid: String, phone: String?): String {
        val now = System.currentTimeMillis()
        val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUid)
        val walletRef = firestore.collection(WALLETS_COLLECTION).document(firebaseUid)
        val existingUser = userRef.get().await()
        val normalizedPhone = normalizePhone(phone)

        val userPayload = buildMap<String, Any> {
            put("id", firebaseUid)
            put("displayName", buildDisplayName(phone, firebaseUid))
            put("email", buildEmail(firebaseUid))
            put("kycApproved", existingUser.getBoolean("kycApproved") ?: false)
            put("isFrozen", existingUser.getBoolean("isFrozen") ?: false)
            put("updatedAt", now)
            put("lastLoginAt", now)
            if (!phone.isNullOrBlank()) {
                put("phone", phone)
            }
            if (!normalizedPhone.isNullOrBlank()) {
                put("phoneNormalized", normalizedPhone)
            }
            if (!existingUser.exists()) {
                put("createdAt", now)
            }
        }

        userRef.set(userPayload, SetOptions.merge()).await()

        if (!walletRef.get().await().exists()) {
            walletRef.set(
                mapOf(
                    "userId" to firebaseUid,
                    "currency" to DEFAULT_CURRENCY,
                    "balance" to 0,
                    "updatedAt" to now,
                ),
                SetOptions.merge(),
            ).await()
        }

        return firebaseUid
    }

    override suspend fun loadHome(userId: String): NobleHomeSnapshot {
        ensureWalletExists(userId)

        val walletSnapshot = firestore.collection(WALLETS_COLLECTION)
            .document(userId)
            .get()
            .await()

        val transactionSnapshots = firestore.collection(TRANSACTIONS_COLLECTION)
            .whereArrayContains("participantIds", userId)
            .get()
            .await()

        val wallet = walletSnapshot.toWallet()
        val transactions = transactionSnapshots.documents
            .mapNotNull { it.toTransaction(ownerUserId = userId) }
            .sortedByDescending { it.createdAt.toEpochMillisOrZero() }

        return NobleHomeSnapshot(
            wallet = wallet,
            transactions = transactions,
        )
    }

    override suspend fun transferMoney(
        fromUserId: String,
        recipient: String,
        amount: Int,
        note: String?,
    ): NobleHomeSnapshot {
        require(amount > 0) { "Enter a valid transfer amount." }

        val toUserId = resolveRecipient(recipient)
        require(fromUserId != toUserId) { "You can't transfer to the same account." }

        val senderUserRef = firestore.collection(USERS_COLLECTION).document(fromUserId)
        val senderWalletRef = firestore.collection(WALLETS_COLLECTION).document(fromUserId)
        val receiverWalletRef = firestore.collection(WALLETS_COLLECTION).document(toUserId)
        val receiverUserRef = firestore.collection(USERS_COLLECTION).document(toUserId)
        val transactionRef = firestore.collection(TRANSACTIONS_COLLECTION).document()
        val eventRef = firestore.collection(ADMIN_EVENTS_COLLECTION).document()
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val senderUser = transaction.get(senderUserRef)
            val receiverUser = transaction.get(receiverUserRef)
            val senderWallet = transaction.get(senderWalletRef)
            val receiverWallet = transaction.get(receiverWalletRef)

            val senderFrozen = senderUser.getBoolean("isFrozen") ?: false
            val receiverFrozen = receiverUser.getBoolean("isFrozen") ?: false
            if (senderFrozen) {
                throw IllegalStateException("Your account is temporarily frozen.")
            }
            if (receiverFrozen) {
                throw IllegalStateException("Recipient account is frozen.")
            }

            val senderBalance = (senderWallet.getLong("balance") ?: 0L).toInt()
            if (senderBalance < amount) {
                throw IllegalStateException("Insufficient balance.")
            }

            val receiverBalance = (receiverWallet.getLong("balance") ?: 0L).toInt()

            transaction.set(
                senderWalletRef,
                mapOf(
                    "userId" to fromUserId,
                    "currency" to DEFAULT_CURRENCY,
                    "balance" to senderBalance - amount,
                    "updatedAt" to now,
                ),
                SetOptions.merge(),
            )
            transaction.set(
                receiverWalletRef,
                mapOf(
                    "userId" to toUserId,
                    "currency" to DEFAULT_CURRENCY,
                    "balance" to receiverBalance + amount,
                    "updatedAt" to now,
                ),
                SetOptions.merge(),
            )
            transaction.set(
                transactionRef,
                mapOf(
                    "id" to transactionRef.id,
                    "fromUserId" to fromUserId,
                    "toUserId" to toUserId,
                    "amount" to amount,
                    "currency" to DEFAULT_CURRENCY,
                    "status" to "completed",
                    "note" to note,
                    "participantIds" to listOf(fromUserId, toUserId),
                    "createdAt" to now,
                ),
            )
            transaction.set(
                eventRef,
                mapOf(
                    "id" to eventRef.id,
                    "action" to "transfer_completed",
                    "targetId" to transactionRef.id,
                    "meta" to "from=$fromUserId,to=$toUserId,amount=$amount",
                    "createdAt" to now,
                ),
            )
        }.await()

        return loadHome(fromUserId)
    }

    override suspend fun topUpWallet(userId: String, amount: Int): NobleWallet {
        require(amount > 0) { "Top up amount must be greater than zero." }

        val walletRef = firestore.collection(WALLETS_COLLECTION).document(userId)
        val transactionRef = firestore.collection(TRANSACTIONS_COLLECTION).document()
        val eventRef = firestore.collection(ADMIN_EVENTS_COLLECTION).document()
        val now = System.currentTimeMillis()

        val newBalance = firestore.runTransaction { transaction ->
            val walletSnapshot = transaction.get(walletRef)
            val currentBalance = (walletSnapshot.getLong("balance") ?: 0L).toInt()
            val updatedBalance = currentBalance + amount

            transaction.set(
                walletRef,
                mapOf(
                    "userId" to userId,
                    "currency" to DEFAULT_CURRENCY,
                    "balance" to updatedBalance,
                    "updatedAt" to now,
                ),
                SetOptions.merge(),
            )
            transaction.set(
                transactionRef,
                mapOf(
                    "id" to transactionRef.id,
                    "fromUserId" to "system",
                    "toUserId" to userId,
                    "amount" to amount,
                    "currency" to DEFAULT_CURRENCY,
                    "status" to "completed",
                    "note" to "Wallet top-up",
                    "participantIds" to listOf(userId),
                    "createdAt" to now,
                ),
            )
            transaction.set(
                eventRef,
                mapOf(
                    "id" to eventRef.id,
                    "action" to "wallet_topup",
                    "targetId" to userId,
                    "meta" to "amount=$amount",
                    "createdAt" to now,
                ),
            )
            updatedBalance
        }.await()

        return NobleWallet(
            userId = userId,
            currency = DEFAULT_CURRENCY,
            balance = newBalance,
            updatedAt = now,
        )
    }

    override suspend fun markKycSubmitted(
        userId: String,
        idHash: String,
        documentNumber: String?,
    ) {
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)
        val eventRef = firestore.collection(ADMIN_EVENTS_COLLECTION).document()
        val now = System.currentTimeMillis()

        userRef.set(
            buildMap<String, Any> {
                put("id", userId)
                put("kycApproved", false)
                put("kycStatus", "pending")
                put("kycIdHash", idHash)
                put("updatedAt", now)
                put("kycSubmittedAt", now)
                if (!documentNumber.isNullOrBlank()) {
                    put("kycDocumentNumber", documentNumber)
                }
            },
            SetOptions.merge(),
        ).await()

        eventRef.set(
            mapOf(
                "id" to eventRef.id,
                "action" to "kyc_submitted",
                "targetId" to userId,
                "meta" to "documentNumber=${documentNumber.orEmpty()}",
                "createdAt" to now,
            ),
        ).await()
    }

    private suspend fun resolveRecipient(recipient: String): String {
        val trimmed = recipient.trim()
        val directSnapshot = firestore.collection(USERS_COLLECTION).document(trimmed).get().await()
        if (directSnapshot.exists()) {
            return directSnapshot.id
        }

        val normalizedPhone = normalizePhone(trimmed)
        if (!normalizedPhone.isNullOrBlank()) {
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("phoneNormalized", normalizedPhone)
                .limit(1)
                .get()
                .await()
            querySnapshot.documents.firstOrNull()?.id?.let { return it }
        }

        throw IllegalStateException("Recipient not found.")
    }

    private suspend fun ensureWalletExists(userId: String) {
        val walletRef = firestore.collection(WALLETS_COLLECTION).document(userId)
        val snapshot = walletRef.get().await()
        if (snapshot.exists()) return

        walletRef.set(
            mapOf(
                "userId" to userId,
                "currency" to DEFAULT_CURRENCY,
                "balance" to 0,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toWallet(): NobleWallet? {
        if (!exists()) return null
        val userId = getString("userId").orEmpty().ifBlank { id }
        return NobleWallet(
            userId = userId,
            currency = getString("currency").orEmpty().ifBlank { DEFAULT_CURRENCY },
            balance = (getLong("balance") ?: 0L).toInt(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis(),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toTransaction(
        ownerUserId: String,
    ): NobleTransaction? {
        val fromUserId = getString("fromUserId") ?: return null
        val toUserId = getString("toUserId") ?: return null
        val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        return NobleTransaction(
            id = getString("id").orEmpty().ifBlank { id },
            ownerUserId = ownerUserId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            amount = (getLong("amount") ?: 0L).toInt(),
            currency = getString("currency").orEmpty().ifBlank { DEFAULT_CURRENCY },
            status = getString("status").orEmpty().ifBlank { "completed" },
            note = getString("note"),
            createdAt = formatCreatedAt(createdAt),
        )
    }

    private fun buildDisplayName(phone: String?, firebaseUid: String): String {
        val suffix = phone?.takeLast(4)?.takeIf { it.isNotBlank() }
            ?: firebaseUid.takeLast(4).uppercase(Locale.ROOT)
        return "Noble User $suffix"
    }

    private fun buildEmail(firebaseUid: String): String = "user-${firebaseUid.takeLast(6)}@noble.app"

    private fun normalizePhone(phone: String?): String? {
        val value = phone?.trim().orEmpty()
        if (value.isBlank()) return null
        return if (value.startsWith("+")) value else "+2$value"
    }

    private fun formatCreatedAt(createdAt: Long): String = DATE_FORMATTER.format(
        Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()),
    )

    private fun String.toEpochMillisOrZero(): Long = runCatching {
        OffsetDateTime.parse(this).toInstant().toEpochMilli()
    }.getOrDefault(0L)

    private companion object {
        const val USERS_COLLECTION = "users"
        const val WALLETS_COLLECTION = "wallets"
        const val TRANSACTIONS_COLLECTION = "transactions"
        const val ADMIN_EVENTS_COLLECTION = "adminEvents"
        const val DEFAULT_CURRENCY = "ETB"

        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }
}
