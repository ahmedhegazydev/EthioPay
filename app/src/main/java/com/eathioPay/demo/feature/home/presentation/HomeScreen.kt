package com.eathioPay.demo.feature.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eathioPay.demo.payment.demo.AdminTab
import com.eathioPay.demo.payment.demo.HistoryTab
import com.eathioPay.demo.payment.demo.TransferTab
import com.eathioPay.demo.payment.demo.WalletTab
import com.eathioPay.demo.payment.models.ScannedCardUi
import com.stripe.android.Stripe

@Immutable
private enum class HomeTab { Wallet, Transfer, History, Admin }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    scannedCard: ScannedCardUi?,
    onScanClick: () -> Unit,
    onLogOutClicked: () -> Unit,
    stripe: Stripe,
    isPaying: Boolean,
    setIsPaying: (Boolean) -> Unit,
    refreshSignal: Int,
    bumpRefresh: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Wallet) }
    var status by remember { mutableStateOf("Idle") }

    val bg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7F8FC), Color(0xFFF2F4FA), Color(0xFFF7F8FC))
    )
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val accent = Color(0xFF4F46E5)
    val accentSoft = Color(0xFFEEF2FF)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Noble Pay Demo", fontWeight = FontWeight.Bold, color = textMain)
                        Text("Demo environment", color = textSub, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentSoft)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = userId.take(1).uppercase(),
                            color = accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "User: ${userId.take(10)}…",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White,
                            labelColor = textSub,
                        ),
                    )
                    IconButton(onClick = onLogOutClicked) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = textMain)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.label) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                androidx.compose.foundation.layout.Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(color = accentSoft, shape = RoundedCornerShape(999.dp)) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (isPaying) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp, color = accent)
                    } else {
                        Text("Ready", color = textSub, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    label = "home_tabs",
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                ) { tab ->
                    when (tab) {
                        HomeTab.Wallet -> WalletTab(
                            userId = userId,
                            onScanClick = onScanClick,
                            scannedCard = scannedCard,
                            stripe = stripe,
                            isPaying = isPaying,
                            setIsPaying = setIsPaying,
                            onStatus = { status = it },
                            refreshSignal = refreshSignal,
                            bumpRefresh = bumpRefresh,
                        )

                        HomeTab.Transfer -> TransferTab(
                            userId = userId,
                            onStatus = { status = it },
                            bumpRefresh = bumpRefresh,
                        )

                        HomeTab.History -> HistoryTab(
                            userId = userId,
                            refreshSignal = refreshSignal,
                            onStatus = { status = it },
                            bumpRefresh = bumpRefresh,
                        )

                        HomeTab.Admin -> AdminTab(
                            onStatus = { status = it },
                            bumpRefresh = bumpRefresh,
                        )
                    }
                }
            }
        }
    }
}

private val HomeTab.label: String
    get() = when (this) {
        HomeTab.Wallet -> "Wallet"
        HomeTab.Transfer -> "Transfer"
        HomeTab.History -> "History"
        HomeTab.Admin -> "Admin"
    }

private val HomeTab.icon
    get() = when (this) {
        HomeTab.Wallet -> Icons.Default.CreditCard
        HomeTab.Transfer -> Icons.Default.Send
        HomeTab.History -> Icons.Default.History
        HomeTab.Admin -> Icons.Default.AdminPanelSettings
    }

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        userId = "demo-user-123",
        scannedCard = null,
        onScanClick = {},
        onLogOutClicked = {},
        stripe = Stripe(androidx.compose.ui.platform.LocalContext.current, "pk_test_preview"),
        isPaying = false,
        setIsPaying = {},
        refreshSignal = 0,
        bumpRefresh = {},
    )
}
