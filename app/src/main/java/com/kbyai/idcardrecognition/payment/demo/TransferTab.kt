package com.kbyai.idcardrecognition.payment.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kbyai.idcardrecognition.noble.ui.home.NobleHomeUiState
import com.kbyai.idcardrecognition.noble.ui.components.NobleInputField
import com.kbyai.idcardrecognition.noble.ui.components.NoblePrimaryButton

@Composable
fun TransferTab(
    state: NobleHomeUiState,
    onRecipientChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Send Money",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Transfer money securely with a clear and simple form designed for quick confirmation.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                NobleInputField(
                    value = state.transferRecipient,
                    onValueChange = onRecipientChange,
                    label = "Recipient ID / account",
                    placeholder = "Enter recipient ID",
                    modifier = Modifier.fillMaxWidth(),
                )

                NobleInputField(
                    value = state.transferAmount,
                    onValueChange = onAmountChange,
                    label = "Amount (ETB)",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                NobleInputField(
                    value = state.transferNote,
                    onValueChange = onNoteChange,
                    label = "Note (optional)",
                    placeholder = "Add a note",
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Available balance: ${state.wallet?.balance ?: 0} ${state.wallet?.currency ?: "ETB"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                NoblePrimaryButton(
                    text = if (state.isTransferSubmitting) "Sending..." else "Send Now",
                    onClick = onSend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.canSubmitTransfer && !state.isTransferSubmitting,
                    isLoading = state.isTransferSubmitting,
                )
            }
        }

        Text(
            text = "All transactions are encrypted and validated before processing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
