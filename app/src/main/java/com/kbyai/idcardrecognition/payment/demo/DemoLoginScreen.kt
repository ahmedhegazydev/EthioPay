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
    var status by remember { mutableStateOf("Ready") }
    val ctx = LocalContext.current

    // 🎨 Minimalist palette for 40-50+ group
    val bg = Color(0xFFF9FAFB) // Very light gray/white
    val textMain = Color(0xFF1F2937)
    val textSub = Color(0xFF4B5563)
    val accent = Color(0xFF1A56BE) // Noble Blue
    val accentSoft = Color(0xFFE0E7FF)

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = textMain
                ),
                title = {
                    Text(
                        "Noble",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp
                    )
                },
                actions = {
                    IconButton(onClick = onLogOutClicked) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = textMain)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = tab == Tab.Wallet,
                    onClick = { tab = Tab.Wallet },
                    label = { Text("Wallet", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accent,
                        selectedTextColor = accent,
                        indicatorColor = accentSoft
                    )
                )
                NavigationBarItem(
                    selected = tab == Tab.Transfer,
                    onClick = { tab = Tab.Transfer },
                    label = { Text("Send", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Send, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accent,
                        selectedTextColor = accent,
                        indicatorColor = accentSoft
                    )
                )
                NavigationBarItem(
                    selected = tab == Tab.History,
                    onClick = { tab = Tab.History },
                    label = { Text("History", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accent,
                        selectedTextColor = accent,
                        indicatorColor = accentSoft
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(Modifier.height(16.dp))

            // Simplified Status Bar
            if (isPaying) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = accent,
                    trackColor = accentSoft
                )
                Text(
                    "Processing...",
                    modifier = Modifier.padding(top = 4.dp),
                    color = accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Main Content
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
