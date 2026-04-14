package com.kbyai.idcardrecognition.noble.data

import com.kbyai.idcardrecognition.noble.data.local.NobleDao
import com.kbyai.idcardrecognition.noble.data.local.TransactionCacheEntity
import com.kbyai.idcardrecognition.noble.data.local.WalletCacheEntity
import com.kbyai.idcardrecognition.noble.data.remote.NobleApiService
import com.kbyai.idcardrecognition.noble.data.remote.TopUpRequest
import com.kbyai.idcardrecognition.noble.data.remote.TransactionEnvelopeResponse
import com.kbyai.idcardrecognition.noble.data.remote.TransactionResponse
import com.kbyai.idcardrecognition.noble.data.remote.TransactionsResponse
import com.kbyai.idcardrecognition.noble.data.remote.TransferRequest
import com.kbyai.idcardrecognition.noble.data.remote.WalletResponse
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
        val repository = NobleRepositoryImpl(api = FakeNobleApiService(), dao = dao)

        val snapshot = repository.loadCachedHome("user-1")

        assertEquals(800, snapshot.wallet?.balance)
        assertEquals("tx-1", snapshot.transactions.single().id)
    }

    @Test
    fun `sync home stores fresh wallet and transactions in cache`() = runTest {
        val api = FakeNobleApiService().apply {
            walletResponse = WalletResponse("user-1", "ETB", 1200)
            transactionsResponse = TransactionsResponse(
                items = listOf(
                    TransactionResponse(
                        id = "tx-2",
                        fromUserId = "user-1",
                        toUserId = "user-3",
                        amount = 400,
                        status = "completed",
                        note = "Rent",
                        createdAt = "2026-04-14T11:00:00Z",
                    )
                )
            )
        }
        val dao = FakeNobleDao()
        val repository = NobleRepositoryImpl(api = api, dao = dao)

        val snapshot = repository.syncHome("user-1")

        assertEquals(1200, snapshot.wallet?.balance)
        assertEquals(1200, dao.wallet?.balance)
        assertEquals(listOf("tx-2"), dao.transactions.map { it.id })
    }

    @Test
    fun `transfer money sends request then reloads latest home snapshot`() = runTest {
        val api = FakeNobleApiService().apply {
            walletResponse = WalletResponse("user-1", "ETB", 600)
            transactionsResponse = TransactionsResponse(
                items = listOf(
                    TransactionResponse(
                        id = "tx-3",
                        fromUserId = "user-1",
                        toUserId = "user-9",
                        amount = 200,
                        status = "completed",
                        note = "Dinner",
                        createdAt = "2026-04-14T12:00:00Z",
                    )
                )
            )
        }
        val dao = FakeNobleDao()
        val repository = NobleRepositoryImpl(api = api, dao = dao)

        val snapshot = repository.transferMoney(
            fromUserId = "user-1",
            toUserId = "user-9",
            amount = 200,
            note = "Dinner",
        )

        assertEquals(
            TransferRequest(
                fromUserId = "user-1",
                toUserId = "user-9",
                amount = 200,
                note = "Dinner",
            ),
            api.lastTransferRequest,
        )
        assertEquals(600, snapshot.wallet?.balance)
        assertEquals("tx-3", snapshot.transactions.single().id)
    }

    @Test
    fun `top up wallet caches returned balance`() = runTest {
        val api = FakeNobleApiService().apply {
            topUpResponse = WalletResponse("user-1", "ETB", 2500)
        }
        val dao = FakeNobleDao()
        val repository = NobleRepositoryImpl(api = api, dao = dao)

        val wallet = repository.topUpWallet("user-1", 500)

        assertEquals(TopUpRequest(500), api.lastTopUpRequest)
        assertEquals(2500, wallet.balance)
        assertEquals(2500, dao.wallet?.balance)
        assertNull(dao.clearedTransactionsFor)
    }

    private class FakeNobleApiService : NobleApiService {
        var walletResponse = WalletResponse("user-1", "ETB", 1000)
        var topUpResponse = walletResponse
        var transactionsResponse = TransactionsResponse(emptyList())
        var lastTransferRequest: TransferRequest? = null
        var lastTopUpRequest: TopUpRequest? = null

        override suspend fun getWallet(userId: String): WalletResponse = walletResponse

        override suspend fun getTransactions(userId: String): TransactionsResponse = transactionsResponse

        override suspend fun transfer(request: TransferRequest): TransactionEnvelopeResponse {
            lastTransferRequest = request
            return TransactionEnvelopeResponse(
                tx = TransactionResponse(
                    id = "transfer-tx",
                    fromUserId = request.fromUserId,
                    toUserId = request.toUserId,
                    amount = request.amount,
                    status = "completed",
                    note = request.note,
                    createdAt = "2026-04-14T12:00:00Z",
                )
            )
        }

        override suspend fun topUp(userId: String, request: TopUpRequest): WalletResponse {
            lastTopUpRequest = request
            return topUpResponse
        }
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
}
