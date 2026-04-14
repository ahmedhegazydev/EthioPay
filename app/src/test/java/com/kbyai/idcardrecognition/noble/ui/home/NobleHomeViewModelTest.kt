package com.kbyai.idcardrecognition.noble.ui.home

import com.kbyai.idcardrecognition.noble.data.NobleRepository
import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.noble.domain.NobleWallet
import com.kbyai.idcardrecognition.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NobleHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `bootstrap loads home from repository and clears loading state`() = runTest {
        val repository = FakeNobleRepository(
            syncedSnapshot = homeSnapshot(balance = 1400, transactionId = "tx-sync"),
        )

        val viewModel = NobleHomeViewModel(userId = "user-1", repository = repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1400, state.wallet?.balance)
        assertEquals("tx-sync", state.transactions.single().id)
        assertFalse(state.isInitialLoading)
        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `bootstrap uses cache first then refreshes with success message`() = runTest {
        val repository = FakeNobleRepository(
            cachedSnapshot = homeSnapshot(balance = 700, transactionId = "tx-cache"),
            syncedSnapshot = homeSnapshot(balance = 950, transactionId = "tx-remote"),
        )

        val viewModel = NobleHomeViewModel(userId = "user-1", repository = repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(950, state.wallet?.balance)
        assertEquals("tx-remote", state.transactions.single().id)
        assertEquals("Data updated", state.message)
        assertEquals(1, repository.loadCachedCalls)
        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `submit transfer validates recipient and amount before repository call`() = runTest {
        val repository = FakeNobleRepository(
            syncedSnapshot = homeSnapshot(balance = 1200, transactionId = "tx-sync"),
        )
        val viewModel = NobleHomeViewModel(userId = "user-1", repository = repository)
        advanceUntilIdle()

        viewModel.onIntent(NobleHomeIntent.SubmitTransfer)
        advanceUntilIdle()

        assertEquals("Enter a valid recipient and amount.", viewModel.state.value.errorMessage)
        assertEquals(0, repository.transferCalls.size)
    }

    @Test
    fun `submit transfer succeeds and clears form state`() = runTest {
        val repository = FakeNobleRepository(
            syncedSnapshot = homeSnapshot(balance = 1500, transactionId = "tx-sync"),
            transferSnapshot = homeSnapshot(balance = 1100, transactionId = "tx-transfer"),
        )
        val viewModel = NobleHomeViewModel(userId = "user-1", repository = repository)
        advanceUntilIdle()

        viewModel.onIntent(NobleHomeIntent.UpdateTransferRecipient(" user-9 "))
        viewModel.onIntent(NobleHomeIntent.UpdateTransferAmount("400"))
        viewModel.onIntent(NobleHomeIntent.UpdateTransferNote("Dinner"))
        viewModel.onIntent(NobleHomeIntent.SubmitTransfer)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("user-9", repository.transferCalls.single().toUserId)
        assertEquals(400, repository.transferCalls.single().amount)
        assertEquals(1100, state.wallet?.balance)
        assertEquals("Money sent successfully.", state.message)
        assertEquals("", state.transferRecipient)
        assertEquals("", state.transferAmount)
        assertEquals("", state.transferNote)
        assertFalse(state.isTransferSubmitting)
    }

    @Test
    fun `submit transfer failure exposes error message`() = runTest {
        val repository = FakeNobleRepository(
            syncedSnapshot = homeSnapshot(balance = 1500, transactionId = "tx-sync"),
            transferFailure = IllegalStateException("Service unavailable"),
        )
        val viewModel = NobleHomeViewModel(userId = "user-1", repository = repository)
        advanceUntilIdle()

        viewModel.onIntent(NobleHomeIntent.UpdateTransferRecipient("user-9"))
        viewModel.onIntent(NobleHomeIntent.UpdateTransferAmount("400"))
        viewModel.onIntent(NobleHomeIntent.SubmitTransfer)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Service unavailable", state.errorMessage)
        assertFalse(state.isTransferSubmitting)
    }

    private data class TransferCall(
        val fromUserId: String,
        val toUserId: String,
        val amount: Int,
        val note: String?,
    )

    private class FakeNobleRepository(
        var cachedSnapshot: NobleHomeSnapshot = NobleHomeSnapshot(null, emptyList()),
        var syncedSnapshot: NobleHomeSnapshot = NobleHomeSnapshot(null, emptyList()),
        var transferSnapshot: NobleHomeSnapshot = syncedSnapshot,
        var transferFailure: Throwable? = null,
    ) : NobleRepository {
        var loadCachedCalls = 0
        var syncCalls = 0
        val transferCalls = mutableListOf<TransferCall>()

        override suspend fun loadCachedHome(userId: String): NobleHomeSnapshot {
            loadCachedCalls++
            return cachedSnapshot
        }

        override suspend fun syncHome(userId: String): NobleHomeSnapshot {
            syncCalls++
            return syncedSnapshot
        }

        override suspend fun transferMoney(
            fromUserId: String,
            toUserId: String,
            amount: Int,
            note: String?,
        ): NobleHomeSnapshot {
            transferCalls += TransferCall(fromUserId, toUserId, amount, note)
            transferFailure?.let { throw it }
            return transferSnapshot
        }

        override suspend fun topUpWallet(userId: String, amount: Int): NobleWallet {
            return syncedSnapshot.wallet ?: NobleWallet(userId = userId, currency = "ETB", balance = amount)
        }
    }

    private fun homeSnapshot(balance: Int, transactionId: String): NobleHomeSnapshot = NobleHomeSnapshot(
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
                toUserId = "user-2",
                amount = 300,
                currency = "ETB",
                status = "completed",
                note = "Note",
                createdAt = "2026-04-14T12:00:00Z",
            )
        ),
    )
}
