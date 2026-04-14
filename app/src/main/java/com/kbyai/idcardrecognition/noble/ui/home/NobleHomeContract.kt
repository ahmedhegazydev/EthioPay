package com.kbyai.idcardrecognition.noble.ui.home

import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.noble.domain.NobleWallet

enum class NobleHomeTab {
    Wallet,
    Send,
    History,
}

data class NobleHomeUiState(
    val userId: String,
    val activeTab: NobleHomeTab = NobleHomeTab.Wallet,
    val wallet: NobleWallet? = null,
    val transactions: List<NobleTransaction> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isTransferSubmitting: Boolean = false,
    val transferRecipient: String = "",
    val transferAmount: String = "",
    val transferNote: String = "",
    val paymentAmount: String = "1000",
    val message: String? = null,
    val errorMessage: String? = null,
) {
    val canSubmitTransfer: Boolean
        get() = transferRecipient.isNotBlank() && (transferAmount.toIntOrNull() ?: 0) > 0
}

sealed interface NobleHomeIntent {
    data class SelectTab(val tab: NobleHomeTab) : NobleHomeIntent
    data class UpdateTransferRecipient(val value: String) : NobleHomeIntent
    data class UpdateTransferAmount(val value: String) : NobleHomeIntent
    data class UpdateTransferNote(val value: String) : NobleHomeIntent
    data class UpdatePaymentAmount(val value: String) : NobleHomeIntent
    data object RefreshRequested : NobleHomeIntent
    data object SubmitTransfer : NobleHomeIntent
    data object ClearFeedback : NobleHomeIntent
}

internal sealed interface NobleHomeChange {
    data class ActiveTabChanged(val tab: NobleHomeTab) : NobleHomeChange
    data class InitialLoadingChanged(val value: Boolean) : NobleHomeChange
    data class RefreshingChanged(val value: Boolean) : NobleHomeChange
    data class TransferSubmittingChanged(val value: Boolean) : NobleHomeChange
    data class SnapshotLoaded(val snapshot: NobleHomeSnapshot) : NobleHomeChange
    data class TransferRecipientChanged(val value: String) : NobleHomeChange
    data class TransferAmountChanged(val value: String) : NobleHomeChange
    data class TransferNoteChanged(val value: String) : NobleHomeChange
    data class PaymentAmountChanged(val value: String) : NobleHomeChange
    data class MessageChanged(val value: String?) : NobleHomeChange
    data class ErrorChanged(val value: String?) : NobleHomeChange
    data class TransferCompleted(
        val snapshot: NobleHomeSnapshot,
        val message: String,
    ) : NobleHomeChange

    data object FeedbackCleared : NobleHomeChange
}
