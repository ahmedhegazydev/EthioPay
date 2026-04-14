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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbyai.idcardrecognition.noble.ui.home.NobleHomeUiState
import com.kbyai.idcardrecognition.noble.ui.components.NobleInputField
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.kbyai.idcardrecognition.payment.stripe.PayWithScannedCard
import com.stripe.android.Stripe
import org.json.JSONObject

@Composable
fun WalletTab(
    state: NobleHomeUiState,
    scannedCard: ScannedCardUi?,
    onScanClick: () -> Unit,
    stripe: Stripe,
    isPaying: Boolean,
    setIsPaying: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onPaymentAmountChange: (String) -> Unit,
    onPrepareTopUp: (Int) -> Unit,
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Wallet Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Track your balance, top up with scanned cards, and keep recent activity close.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current balance",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                Text(
                    text = "${state.wallet?.balance ?: 0} ${state.wallet?.currency ?: "ETB"}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                Text(
                    text = "User ID: ${state.userId.take(8).uppercase()}",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan New Card")
            }

            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
        }

        if (scannedCard != null) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Pay with scanned card",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Confirm the amount first, then complete the secure Stripe payment flow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    NobleInputField(
                        value = state.paymentAmount,
                        onValueChange = onPaymentAmountChange,
                        label = "Amount to pay",
                        placeholder = "1000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    PayWithScannedCard(
                        scanned = scannedCard,
                        onCreatePaymentIntent = {
                            val amount = state.paymentAmount.toIntOrNull()?.takeIf { it > 0 } ?: 1000
                            onPrepareTopUp(amount)
                            val response = DemoApiRaw.postJson(
                                "${DemoApi.baseUrl}/create-payment-intent",
                                """{"amount":$amount,"currency":"usd"}"""
                            )
                            JSONObject(response).getString("clientSecret")
                        },
                        onStatus = { },
                        stripe = stripe,
                        isPaying = isPaying,
                        setIsPaying = setIsPaying,
                    )
                }
            }
        }

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Recent activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                if (state.transactions.isEmpty()) {
                    Text(
                        text = if (state.isInitialLoading) "Loading activity..." else "No transactions yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    state.transactions.take(3).forEach { transaction ->
                        NobleTransactionCard(transaction = transaction)
                    }
                }
            }
        }
    }
}
