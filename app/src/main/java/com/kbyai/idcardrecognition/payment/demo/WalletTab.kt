package com.kbyai.idcardrecognition.payment.demo

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Brush
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
import androidx.fragment.app.FragmentActivity
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

    // 🎨 Light palette
    val pageBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7F8FC), Color(0xFFF2F4FA), Color(0xFFF7F8FC))
    )
    val cardBg = Color.White
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val border = Color(0xFFE5E7EB)
    val accent = Color(0xFF4F46E5)     // Indigo
    val accentSoft = Color(0xFFEEF2FF)

    fun refresh() {
        scope.launch {
            try {
                loadingWallet = true
                onStatus("Loading wallet…")
                wallet = DemoApi.getWallet(userId)
                onStatus("OK")
            } catch (e: Exception) {
                onStatus("Error: ${e.message}")
            } finally {
                loadingWallet = false
            }
        }
    }


    LaunchedEffect(userId, refreshSignal) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)   // ✅
            .background(pageBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ===== Header / Balance Card =====
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accentSoft, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Wallet, contentDescription = null, tint = accent)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Wallet",
                            color = textMain,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    clipboard.setText(AnnotatedString("Wallet"))
                                    Toast.makeText(ctx, "Copied ✅", Toast.LENGTH_SHORT).show()
                                }
                            )
                        )
                        Text(
                            "Balance & top up",
                            color = textSub,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    FilledTonalIconButton(onClick = { refresh() }, enabled = !loadingWallet) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = accent)
                    }
                }

                // UserId row
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UserId: ${userId.take(18)}…",
                            color = textSub,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(userId))
                                Toast.makeText(ctx, "UserId copied ✅", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = accent)
                        }
                    }
                }

                // Balance
                val bal = wallet?.balance ?: 0
                val cur = wallet?.currency ?: "ETB"
                Text(
                    text = "$bal $cur",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textMain
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Currency: $cur") },


                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = accentSoft,
                            labelColor = accent
                        ),
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text(if (loadingWallet) "Updating…" else "Up to date") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFF3F4F6),
                            labelColor = textSub
                        ),
                    )
                }

                if (loadingWallet) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = accent,
                        trackColor = accentSoft
                    )
                }
            }
        }

        // ===== Quick actions =====
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text("Quick TopUp (Demo)", color = textMain, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickChipLight("500") { topupAmount = "500" }
                    QuickChipLight("1000") { topupAmount = "1000" }
                    QuickChipLight("5000") { topupAmount = "5000" }
                    QuickChipLight("10000") { topupAmount = "10000" }
                }

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                DemoApi.topup(userId, 1000)
                                bumpRefresh()
                                Toast.makeText(ctx, "Topup +1000 ETB ✅", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(ctx, e.message ?: "Topup failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Topup 1000 ETB (Demo) 💰", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    onClick = onScanClick,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Scan Card", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ===== Payment Section (only after scan) =====
        if (scannedCard != null) {
//        if (true) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pay with scanned card", color = textMain, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = topupAmount,
                        onValueChange = { topupAmount = it.filter(Char::isDigit).take(9) },
                        label = { Text("Topup Amount (Int)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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

                    if (isPaying) {
                        Text("Processing payment…", color = textSub, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Scan a card first to pay.",
                    modifier = Modifier.padding(14.dp),
                    color = textSub,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun QuickChipLight(label: String, onPick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onPick,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        shape = RoundedCornerShape(999.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFF3F4F6),
            labelColor = Color(0xFF111827)
        ),
    )
}
