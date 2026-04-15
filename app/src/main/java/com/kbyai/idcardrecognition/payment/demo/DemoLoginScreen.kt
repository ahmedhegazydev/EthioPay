package com.kbyai.idcardrecognition.payment.demo

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.stripe.android.Stripe

private enum class Tab { Wallet, Transfer, History, Admin }

@OptIn(ExperimentalMaterial3Api::class)
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
) {
    var tab by remember { mutableStateOf(Tab.Wallet) }
    var status by remember { mutableStateOf("Idle") }
    val ctx = LocalContext.current

    // 🎨 Light palette
    val bg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7F8FC), Color(0xFFF2F4FA), Color(0xFFF7F8FC))
    )
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val accent = Color(0xFF4F46E5)
    val accentSoft = Color(0xFFEEF2FF)
    val border = Color(0xFFE5E7EB)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Noble Pay Demo",
                            fontWeight = FontWeight.Bold,
                            color = textMain
                        )
                        Text(
                            "Demo environment",
                            color = textSub,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    // simple avatar
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userId.take(1).uppercase(),
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // UserId chip
                    AssistChip(
                        onClick = { /* optional copy later */ },
                        label = {
                            Text(
                                "User: ${userId.take(10)}…",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White,
                            labelColor = textSub
                        ),
//                        border = AssistChipDefaults.assistChipBorder(borderColor = border)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onLogOutClicked) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = textMain)
                    }
                    Spacer(Modifier.width(6.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.Wallet,
                    onClick = { tab = Tab.Wallet },
                    label = { Text("Wallet") },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.Transfer,
                    onClick = { tab = Tab.Transfer },
                    label = { Text("Transfer") },
                    icon = { Icon(Icons.Default.Send, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.History,
                    onClick = { tab = Tab.History },
                    label = { Text("History") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.Admin,
                    onClick = { tab = Tab.Admin },
                    label = { Text("Admin") },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ✅ Status bar (pretty)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = accentSoft,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    if (isPaying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = accent
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Processing payment…", color = textSub, fontSize = 12.sp)
                    } else {
                        Text("Ready", color = textSub, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ✅ IMPORTANT: bounded content area (prevents infinity height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (tab) {
                    Tab.Wallet -> WalletTab(
                        userId = userId,
                        onScanClick = onScanClick,
                        scannedCard = scannedCard,
                        stripe = stripe,
                        isPaying = isPaying,
                        setIsPaying = setIsPaying,
                        onStatus = { status = it },
                        refreshSignal = refreshSignal,
                        bumpRefresh = bumpRefresh
                    )

                    Tab.Transfer -> TransferTab(
                        userId = userId,
                        onStatus = { status = it },
                        bumpRefresh = bumpRefresh
                    )

                    Tab.History -> HistoryTab(
                        userId = userId,
                        refreshSignal = refreshSignal,
                        onStatus = { status = it },
                        bumpRefresh = bumpRefresh
                    )

                    Tab.Admin -> AdminTab(
                        onStatus = { status = it },
                        bumpRefresh = bumpRefresh
                    )
                }
            }
        }
    }
}
