package com.kbyai.idcardrecognition.noble.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbyai.idcardrecognition.noble.data.NobleRepository
import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NobleHomeViewModel(
    private val userId: String,
    private val repository: NobleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NobleHomeUiState(userId = userId))
    val state: StateFlow<NobleHomeUiState> = _state

    init {
        bootstrap()
    }

    fun onIntent(intent: NobleHomeIntent) {
        when (intent) {
            is NobleHomeIntent.SelectTab -> applyChange(NobleHomeChange.ActiveTabChanged(intent.tab))
            is NobleHomeIntent.UpdateTransferRecipient -> applyChange(
                NobleHomeChange.TransferRecipientChanged(intent.value)
            )

            is NobleHomeIntent.UpdateTransferAmount -> applyChange(
                NobleHomeChange.TransferAmountChanged(intent.value)
            )

            is NobleHomeIntent.UpdateTransferNote -> applyChange(
                NobleHomeChange.TransferNoteChanged(intent.value)
            )

            is NobleHomeIntent.UpdatePaymentAmount -> applyChange(
                NobleHomeChange.PaymentAmountChanged(intent.value)
            )

            NobleHomeIntent.RefreshRequested -> refreshHome(initial = false)
            NobleHomeIntent.SubmitTransfer -> submitTransfer()
            NobleHomeIntent.ClearFeedback -> applyChange(NobleHomeChange.FeedbackCleared)
        }
    }

    private fun bootstrap() {
        viewModelScope.launch {
            val cached = repository.loadCachedHome(userId)
            val hasCachedContent = cached.wallet != null || cached.transactions.isNotEmpty()
            if (hasCachedContent) {
                applyChange(NobleHomeChange.SnapshotLoaded(cached))
            }
            refreshHome(initial = !hasCachedContent)
        }
    }

    private fun refreshHome(initial: Boolean) {
        viewModelScope.launch {
            if (initial) {
                applyChange(NobleHomeChange.InitialLoadingChanged(true))
            } else {
                applyChange(NobleHomeChange.RefreshingChanged(true))
            }

            runCatching { repository.syncHome(userId) }
                .onSuccess { snapshot ->
                    applyChange(NobleHomeChange.SnapshotLoaded(snapshot))
                    if (!initial) {
                        applyChange(NobleHomeChange.MessageChanged("Data updated"))
                    }
                }
                .onFailure { throwable ->
                    applyChange(NobleHomeChange.InitialLoadingChanged(false))
                    applyChange(NobleHomeChange.RefreshingChanged(false))
                    applyChange(
                        NobleHomeChange.ErrorChanged(
                            throwable.message ?: "Unable to load your wallet right now."
                        )
                    )
                }
        }
    }

    private fun submitTransfer() {
        val current = _state.value
        val amount = current.transferAmount.toIntOrNull()
        if (current.transferRecipient.isBlank() || amount == null || amount <= 0) {
            applyChange(NobleHomeChange.ErrorChanged("Enter a valid recipient and amount."))
            return
        }

        viewModelScope.launch {
            applyChange(NobleHomeChange.TransferSubmittingChanged(true))
            runCatching {
                repository.transferMoney(
                    fromUserId = userId,
                    toUserId = current.transferRecipient,
                    amount = amount,
                    note = current.transferNote.ifBlank { "Transfer" },
                )
            }.onSuccess { snapshot ->
                applyChange(
                    NobleHomeChange.TransferCompleted(
                        snapshot = snapshot,
                        message = "Money sent successfully.",
                    )
                )
            }.onFailure { throwable ->
                applyChange(NobleHomeChange.TransferSubmittingChanged(false))
                applyChange(
                    NobleHomeChange.ErrorChanged(
                        throwable.message ?: "Transfer failed. Please try again."
                    )
                )
            }
        }
    }

    private fun applyChange(change: NobleHomeChange) {
        _state.update { current -> NobleHomeReducer.reduce(current, change) }
    }
}
