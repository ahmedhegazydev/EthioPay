package com.kbyai.idcardrecognition.noble.ui.home

import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.noble.domain.NobleWallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NobleHomeReducerTest {

    @Test
    fun `transfer amount change keeps digits only`() {
        val initial = NobleHomeUiState(userId = "user-1")

        val reduced = NobleHomeReducer.reduce(
            initial,
            NobleHomeChange.TransferAmountChanged("12ab3-4"),
        )

        assertEquals("1234", reduced.transferAmount)
    }

    @Test
    fun `transfer completed clears form and updates wallet`() {
        val initial = NobleHomeUiState(
            userId = "user-1",
            transferRecipient = "user-2",
            transferAmount = "500",
            transferNote = "Dinner",
            isTransferSubmitting = true,
        )

        val snapshot = NobleHomeSnapshot(
            wallet = NobleWallet(userId = "user-1", currency = "ETB", balance = 900),
            transactions = listOf(
                NobleTransaction(
                    id = "tx-1",
                    ownerUserId = "user-1",
                    fromUserId = "user-1",
                    toUserId = "user-2",
                    amount = 500,
                    currency = "ETB",
                    status = "completed",
                    note = "Dinner",
                    createdAt = "2026-04-14T12:00:00Z",
                )
            ),
        )

        val reduced = NobleHomeReducer.reduce(
            initial,
            NobleHomeChange.TransferCompleted(
                snapshot = snapshot,
                message = "Money sent successfully.",
            ),
        )

        assertEquals(900, reduced.wallet?.balance)
        assertEquals("", reduced.transferRecipient)
        assertEquals("", reduced.transferAmount)
        assertEquals("", reduced.transferNote)
        assertFalse(reduced.isTransferSubmitting)
        assertNull(reduced.errorMessage)
        assertEquals("Money sent successfully.", reduced.message)
    }

    @Test
    fun `snapshot loaded clears loading and error states`() {
        val initial = NobleHomeUiState(
            userId = "user-1",
            isInitialLoading = true,
            isRefreshing = true,
            errorMessage = "old error",
        )
        val snapshot = NobleHomeSnapshot(
            wallet = NobleWallet(userId = "user-1", currency = "ETB", balance = 1200),
            transactions = emptyList(),
        )

        val reduced = NobleHomeReducer.reduce(
            initial,
            NobleHomeChange.SnapshotLoaded(snapshot),
        )

        assertEquals(1200, reduced.wallet?.balance)
        assertFalse(reduced.isInitialLoading)
        assertFalse(reduced.isRefreshing)
        assertNull(reduced.errorMessage)
    }

    @Test
    fun `payment amount change strips non digit characters`() {
        val reduced = NobleHomeReducer.reduce(
            NobleHomeUiState(userId = "user-1", errorMessage = "error"),
            NobleHomeChange.PaymentAmountChanged("5a0 0"),
        )

        assertEquals("500", reduced.paymentAmount)
        assertNull(reduced.errorMessage)
    }

    @Test
    fun `ui state can submit transfer only when recipient and positive amount are present`() {
        val valid = NobleHomeUiState(
            userId = "user-1",
            transferRecipient = "user-2",
            transferAmount = "450",
        )
        val invalid = valid.copy(transferAmount = "0")

        assertTrue(valid.canSubmitTransfer)
        assertFalse(invalid.canSubmitTransfer)
    }
}
