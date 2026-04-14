package com.kbyai.idcardrecognition.noble.data

import com.kbyai.idcardrecognition.noble.data.local.NobleDao
import com.kbyai.idcardrecognition.noble.data.local.TransactionCacheEntity
import com.kbyai.idcardrecognition.noble.data.local.WalletCacheEntity
import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.noble.domain.NobleWallet
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NobleRepositoryImplTest {

    @Test
    fun `load cached home maps dao entities to domain`() = runTest {
        val dao = FakeNobleDao().apply {
            wallet = WalletCacheEntity("user-1", "ETB", 800, 1234L)
            transactions = mutableListOf(
                TransactionCacheEntity(
                    id = "tx-1",
                    ownerUserId = "user-1",
                    fromUserId = "user-2",
                    toUserId = "user-1",
                    amount = 150,
                    currency = "ETB",
                    status = "completed",
                    note = "Cash in",
                    createdAt = "2026-04-14T10:00:00Z",
                )
            )
        }
        val repository = NobleRepositoryImpl(cloudStore = FakeNobleCloudStore(), dao = dao)

        val snapshot = repository.loadCachedHome("user-1")

        assertEquals(800, snapshot.wallet?.balance)
        assertEquals("tx-1", snapshot.transactions.single().id)
    }

    @Test
    fun `sync home stores fresh wallet and transactions in cache`() = runTest {
        val cloudStore = FakeNobleCloudStore().apply {
            syncedSnapshot = homeSnapshot(balance = 1200, transactionId = "tx-2")
        }
        val dao = FakeNobleDao()
        val repository = NobleRepositoryImpl(cloudStore = cloudStore, dao = dao)

        val snapshot = repository.syncHome("user-1")

        assertEquals(1200, snapshot.wallet?.balance)
        assertEquals(1200, dao.wallet?.balance)
        assertEquals(listOf("tx-2"), dao.transactions.map { it.id })
    }

    @Test
    fun `transfer money sends request then reloads latest home snapshot`() = runTest {
        val cloudStore = FakeNobleCloudStore().apply {
            transferSnapshot = homeSnapshot(balance = 600, transactionId = "tx-3")
        }
        val dao = FakeNobleDao()
        val repository = NobleRepositoryImpl(cloudStore = cloudStore, dao = dao)

        val snapshot = repository.transferMoney(
            fromUserId = "user-1",
            toUserId = "user-9",
            amount = 200,
            note = "Dinner",
        )

        assertEquals(
            TransferCall(
                fromUserId = "user-1",
                toUserId = "user-9",
                amount = 200,
                note = "Dinner",
            ),
            cloudStore.lastTransferCall,
        )
        assertEquals(600, snapshot.wallet?.balance)
        assertEquals("tx-3", snapshot.transactions.single().id)
    }

    @Test
    fun `top up wallet caches returned balance`() = runTest {
        val cloudStore = FakeNobleCloudStore().apply {
            topUpWallet = NobleWallet("user-1", "ETB", 2500, 1713270000L)
        }
        val dao = FakeNobleDao()
        val repository = NobleRepositoryImpl(cloudStore = cloudStore, dao = dao)

        val wallet = repository.topUpWallet("user-1", 500)

        assertEquals(TopUpCall(userId = "user-1", amount = 500), cloudStore.lastTopUpCall)
        assertEquals(2500, wallet.balance)
        assertEquals(2500, dao.wallet?.balance)
        assertNull(dao.clearedTransactionsFor)
    }

    private data class TransferCall(
        val fromUserId: String,
        val toUserId: String,
        val amount: Int,
        val note: String?,
    )

    private data class TopUpCall(
        val userId: String,
        val amount: Int,
    )

    private class FakeNobleCloudStore : NobleCloudStore {
        var syncedSnapshot: NobleHomeSnapshot = homeSnapshot(balance = 1000, transactionId = "tx-1")
        var transferSnapshot: NobleHomeSnapshot = syncedSnapshot
        var topUpWallet: NobleWallet = NobleWallet("user-1", "ETB", 1000, 1713270000L)
        var lastTransferCall: TransferCall? = null
        var lastTopUpCall: TopUpCall? = null

        override suspend fun ensureUserProfile(firebaseUid: String, phone: String?): String = firebaseUid

        override suspend fun loadHome(userId: String): NobleHomeSnapshot = syncedSnapshot

        override suspend fun transferMoney(
            fromUserId: String,
            recipient: String,
            amount: Int,
            note: String?,
        ): NobleHomeSnapshot {
            lastTransferCall = TransferCall(fromUserId, recipient, amount, note)
            return transferSnapshot
        }

        override suspend fun topUpWallet(userId: String, amount: Int): NobleWallet {
            lastTopUpCall = TopUpCall(userId, amount)
            return topUpWallet
        }

        override suspend fun markKycSubmitted(userId: String, idHash: String, documentNumber: String?) = Unit
    }

    private class FakeNobleDao : NobleDao {
        var wallet: WalletCacheEntity? = null
        var transactions = mutableListOf<TransactionCacheEntity>()
        var clearedTransactionsFor: String? = null

        override suspend fun getWallet(userId: String): WalletCacheEntity? = wallet?.takeIf { it.userId == userId }

        override suspend fun upsertWallet(wallet: WalletCacheEntity) {
            this.wallet = wallet
        }

        override suspend fun getTransactions(userId: String): List<TransactionCacheEntity> =
            transactions.filter { it.ownerUserId == userId }

        override suspend fun clearTransactions(userId: String) {
            clearedTransactionsFor = userId
            transactions.removeAll { it.ownerUserId == userId }
        }

        override suspend fun upsertTransactions(items: List<TransactionCacheEntity>) {
            transactions.addAll(items)
        }
    }

    private companion object {
        fun homeSnapshot(balance: Int, transactionId: String): NobleHomeSnapshot = NobleHomeSnapshot(
            wallet = NobleWallet(
                userId = "user-1",
                currency = "ETB",
                balance = balance,
                updatedAt = 1713090000L,
            ),
            transactions = listOf(
                NobleTransaction(
                    id = transactionId,
                    ownerUserId = "user-1",
                    fromUserId = "user-1",
                    toUserId = "user-3",
                    amount = 400,
                    currency = "ETB",
                    status = "completed",
                    note = "Rent",
                    createdAt = "2026-04-14T11:00:00Z",
                )
            ),
        )
    }
}
