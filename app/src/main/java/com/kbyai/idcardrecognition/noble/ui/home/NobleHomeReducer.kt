package com.kbyai.idcardrecognition.noble.ui.home

internal object NobleHomeReducer {

    fun reduce(
        previous: NobleHomeUiState,
        change: NobleHomeChange,
    ): NobleHomeUiState = when (change) {
        is NobleHomeChange.ActiveTabChanged -> previous.copy(activeTab = change.tab)
        is NobleHomeChange.InitialLoadingChanged -> previous.copy(isInitialLoading = change.value)
        is NobleHomeChange.RefreshingChanged -> previous.copy(isRefreshing = change.value)
        is NobleHomeChange.TransferSubmittingChanged -> previous.copy(isTransferSubmitting = change.value)
        is NobleHomeChange.TransferRecipientChanged -> previous.copy(
            transferRecipient = change.value.trim(),
            errorMessage = null,
            message = null,
        )

        is NobleHomeChange.TransferAmountChanged -> previous.copy(
            transferAmount = change.value.filter(Char::isDigit),
            errorMessage = null,
            message = null,
        )

        is NobleHomeChange.TransferNoteChanged -> previous.copy(
            transferNote = change.value,
            errorMessage = null,
            message = null,
        )

        is NobleHomeChange.PaymentAmountChanged -> previous.copy(
            paymentAmount = change.value.filter(Char::isDigit),
            errorMessage = null,
        )

        is NobleHomeChange.SnapshotLoaded -> previous.copy(
            wallet = change.snapshot.wallet,
            transactions = change.snapshot.transactions,
            isInitialLoading = false,
            isRefreshing = false,
            errorMessage = null,
        )

        is NobleHomeChange.MessageChanged -> previous.copy(message = change.value)
        is NobleHomeChange.ErrorChanged -> previous.copy(errorMessage = change.value)
        NobleHomeChange.FeedbackCleared -> previous.copy(message = null, errorMessage = null)
        is NobleHomeChange.TransferCompleted -> previous.copy(
            wallet = change.snapshot.wallet,
            transactions = change.snapshot.transactions,
            isTransferSubmitting = false,
            transferRecipient = "",
            transferAmount = "",
            transferNote = "",
            message = change.message,
            errorMessage = null,
        )
    }
}
