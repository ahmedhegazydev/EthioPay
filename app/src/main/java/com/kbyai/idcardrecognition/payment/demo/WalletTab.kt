package com.kbyai.idcardrecognition.payment.demo

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.kbyai.idcardrecognition.payment.stripe.PayWithScannedCard
import com.stripe.android.Stripe
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun WalletTab(
    userId: String,
    onScanClick: () -> Unit,
    scannedCard: ScannedCardUi?,
    stripe: Stripe,
    isPaying: Boolean,
    setIsPaying: (Boolean) -> Unit,
    onStatus: (String) -> Unit,
    refreshSignal: Int,
    bumpRefresh: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var wallet by remember { mutableStateOf<WalletDto?>(null) }
    var topupAmount by remember { mutableStateOf("1000") }
    var loadingWallet by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    // 🎨 Noble Theme - Minimalist & Accessible
    val cardBg = Color.White
    val textMain = Color(0xFF1F2937)
    val textSub = Color(0xFF6B7280)
    val border = Color(0xFFE5E7EB)
    val nobleBlue = Color(0xFF1A56BE)
    val nobleBlueSoft = Color(0xFFF0F7FF)

    fun refresh() {
        scope.launch {
            try {
                loadingWallet = true
                onStatus("Refreshing...")
                wallet = DemoApi.getWallet(userId)
                onStatus("Ready")
            } catch (e: Exception) {
                onStatus("Error loading")
            } finally {
                loadingWallet = false
            }
        }
    }

    LaunchedEffect(userId, refreshSignal) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Balance Card - Large & Clear
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Current Balance",
                    color = textSub,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.height(8.dp))
                
                val bal = wallet?.balance ?: 0
                val cur = wallet?.currency ?: "ETB"
                Text(
                    text = "$bal $cur",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = nobleBlue
                )

                if (loadingWallet) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.width(100.dp).height(2.dp),
                        color = nobleBlue,
                        trackColor = nobleBlueSoft
                    )
                }
            }
        }

        // Action Buttons - Large touch targets for 40-50+
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = nobleBlue)
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Scan New Card", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { refresh() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, nobleBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = nobleBlue)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Refresh Balance", fontSize = 18.sp)
            }
        }

        // Payment Section
        if (scannedCard != null) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = nobleBlueSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Pay with Scanned Card",
                        color = nobleBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = topupAmount,
                        onValueChange = { topupAmount = it.filter(Char::isDigit) },
                        label = { Text("Amount to pay") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    )

                    PayWithScannedCard(
                        scanned = scannedCard,
                        onCreatePaymentIntent = {
                            val amt = topupAmount.toIntOrNull() ?: 1000
                            val respStr = DemoApiRaw.postJson(
                                "${DemoApi.baseUrl}/create-payment-intent",
                                """{"amount":$amt,"currency":"usd"}"""
                            )
                            val obj = JSONObject(respStr)
                            obj.getString("clientSecret")
                        },
                        onStatus = onStatus,
                        stripe = stripe,
                        isPaying = isPaying,
                        setIsPaying = setIsPaying
                    )
                }
            }
        }

        // Footer Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ID: ${userId.take(8).uppercase()}",
                color = textSub,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
            Text(
                "Noble Secure Payment System",
                color = textSub.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
