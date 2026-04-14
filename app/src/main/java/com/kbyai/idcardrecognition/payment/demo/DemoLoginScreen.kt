package com.kbyai.idcardrecognition.payment.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kbyai.idcardrecognition.noble.ui.home.NobleHomeIntent
import com.kbyai.idcardrecognition.noble.ui.home.NobleHomeTab
import com.kbyai.idcardrecognition.noble.ui.home.NobleHomeViewModel
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.stripe.android.Stripe
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeDemo(
    userId: String,
    scannedCard: ScannedCardUi?,
    onScanClick: () -> Unit,
    onLogOutClicked: () -> Unit,
    stripe: Stripe,
    isPaying: Boolean,
    setIsPaying: (Boolean) -> Unit,
    refreshSignal: Int,
    bumpRefresh: () -> Unit,
    setLastTopupAmount: (Int) -> Unit,
) {
    val viewModel: NobleHomeViewModel = koinViewModel(parameters = { parametersOf(userId) })
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(refreshSignal) {
        if (refreshSignal > 0) {
            viewModel.onIntent(NobleHomeIntent.RefreshRequested)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Noble",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Secure wallet and payments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogOutClicked) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = state.activeTab == NobleHomeTab.Wallet,
                    onClick = { viewModel.onIntent(NobleHomeIntent.SelectTab(NobleHomeTab.Wallet)) },
                    label = { Text("Wallet") },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
                NavigationBarItem(
                    selected = state.activeTab == NobleHomeTab.Send,
                    onClick = { viewModel.onIntent(NobleHomeIntent.SelectTab(NobleHomeTab.Send)) },
                    label = { Text("Send") },
                    icon = { Icon(Icons.Default.Send, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
                NavigationBarItem(
                    selected = state.activeTab == NobleHomeTab.History,
                    onClick = { viewModel.onIntent(NobleHomeIntent.SelectTab(NobleHomeTab.History)) },
                    label = { Text("History") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.message != null || state.errorMessage != null) {
                FeedbackCard(
                    text = state.errorMessage ?: state.message.orEmpty(),
                    isError = state.errorMessage != null,
                    onDismiss = { viewModel.onIntent(NobleHomeIntent.ClearFeedback) },
                )
            }

            if (isPaying) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (state.activeTab) {
                    NobleHomeTab.Wallet -> WalletTab(
                        state = state,
                        scannedCard = scannedCard,
                        onScanClick = onScanClick,
                        stripe = stripe,
                        isPaying = isPaying,
                        setIsPaying = setIsPaying,
                        onRefresh = { viewModel.onIntent(NobleHomeIntent.RefreshRequested) },
                        onPaymentAmountChange = {
                            viewModel.onIntent(NobleHomeIntent.UpdatePaymentAmount(it))
                        },
                        onPrepareTopUp = { setLastTopupAmount(it) },
                    )

                    NobleHomeTab.Send -> TransferTab(
                        state = state,
                        onRecipientChange = {
                            viewModel.onIntent(NobleHomeIntent.UpdateTransferRecipient(it))
                        },
                        onAmountChange = {
                            viewModel.onIntent(NobleHomeIntent.UpdateTransferAmount(it))
                        },
                        onNoteChange = {
                            viewModel.onIntent(NobleHomeIntent.UpdateTransferNote(it))
                        },
                        onSend = { viewModel.onIntent(NobleHomeIntent.SubmitTransfer) },
                    )

                    NobleHomeTab.History -> HistoryTab(
                        state = state,
                        onRefresh = { viewModel.onIntent(NobleHomeIntent.RefreshRequested) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    text: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Surface(color = containerColor) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = text,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(
                        text = "Dismiss",
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
