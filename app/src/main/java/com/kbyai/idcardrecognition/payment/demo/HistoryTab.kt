package com.kbyai.idcardrecognition.payment.demo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kbyai.idcardrecognition.payment.demo.DemoApi
import com.kbyai.idcardrecognition.payment.demo.TxDto
import kotlinx.coroutines.launch

@Composable
fun HistoryTab(
    userId: String,
    refreshSignal: Int,
    onStatus: (String) -> Unit,
    bumpRefresh: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var items by remember { mutableStateOf<List<TxDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Idle") }

    fun refresh() {
        scope.launch {
            try {
                isLoading = true
                statusText = "Loading transactions…"
                onStatus(statusText)
                items = DemoApi.getTransactions(userId)
                statusText = "Up to date"
                onStatus("OK")
            } catch (e: Exception) {
                statusText = "Error: ${e.message}"
                onStatus(statusText)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId, refreshSignal) { refresh() }

    // 🎨 Light palette
    val pageBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7F8FC), Color(0xFFF2F4FA), Color(0xFFF7F8FC))
    )
    val cardBg = Color.White
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val border = Color(0xFFE5E7EB)
    val accent = Color(0xFF4F46E5)   // Indigo
    val accentSoft = Color(0xFFEEF2FF)
    val divider = Color(0xFFEAECEF)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(pageBg) // لو parent ما بيحطش خلفية
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(pageBg)
                .heightIn(min = 1.dp, max = 900.dp), // ✅ يمنع infinity
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ✅ HEADER
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(accentSoft, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = accent
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Transaction History",
                                    color = textMain,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Last 50 transactions",
                                    color = textSub,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { refresh(); bumpRefresh() },
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = accent)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusChipLight(
                                statusText = statusText,
                                isLoading = isLoading,
                                accent = accent
                            )

                            Text(
                                text = "User: ${userId.take(10)}…",
                                color = textSub,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = accent,
                                trackColor = accentSoft
                            )
                        }
                    }
                }
            }

            // ✅ Empty
            if (!isLoading && items.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No transactions yet", color = textMain, fontWeight = FontWeight.Bold)
                            Text(
                                "Once you top up or transfer, your activity will appear here.",
                                color = textSub
                            )
                        }
                    }
                }
            } else {
                items(items.take(50)) { tx ->
                    TxRowCardLight(
                        tx = tx,
                        accent = accent,
                        accentSoft = accentSoft,
                        textMain = textMain,
                        textSub = textSub,
                        divider = divider,
                        onCopy = { label, value ->
                            clipboard.setText(AnnotatedString(value))
                            Toast.makeText(ctx, "$label copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChipLight(
    statusText: String,
    isLoading: Boolean,
    accent: Color
) {
    val (bg, fg) = when {
        isLoading -> Color(0xFFFFF7ED) to Color(0xFFB45309) // amber-ish
        statusText.startsWith("Error", true) -> Color(0xFFFEF2F2) to Color(0xFFB91C1C)
        statusText.contains("up to date", true) || statusText == "OK" ->
            Color(0xFFECFDF5) to Color(0xFF047857)
        else -> Color(0xFFEEF2FF) to accent
    }

    Surface(color = bg, shape = RoundedCornerShape(999.dp)) {
        Text(
            text = statusText,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TxRowCardLight(
    tx: TxDto,
    accent: Color,
    accentSoft: Color,
    textMain: Color,
    textSub: Color,
    divider: Color,
    onCopy: (label: String, value: String) -> Unit
) {
    val isSuccess = tx.status.equals("succeeded", true) || tx.status.equals("success", true)

    val statusColor = when {
        isSuccess -> Color(0xFF047857)       // green
        tx.status.contains("fail", true) || tx.status.contains("error", true) -> Color(0xFFB91C1C)
        else -> Color(0xFFB45309)            // amber
    }
    val statusBg = when {
        isSuccess -> Color(0xFFECFDF5)
        tx.status.contains("fail", true) || tx.status.contains("error", true) -> Color(0xFFFEF2F2)
        else -> Color(0xFFFFF7ED)
    }

    val isIncoming = tx.note?.contains("topup", true) == true || tx.fromUserId.startsWith("system", true)
    val icon = if (isIncoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val label = if (isIncoming) "Incoming" else "Outgoing"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accentSoft, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "$label • ${tx.amount} ${tx.currency}",
                        color = textMain,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        tx.createdAt,
                        color = textSub,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Surface(color = statusBg, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        tx.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        maxLines = 1
                    )
                }
            }

            Divider(color = divider)

            CopyableLineLight(
                title = "From",
                value = tx.fromUserId,
                accent = accent,
                textMain = textMain,
                textSub = textSub,
                onCopy = { onCopy("From UserId", tx.fromUserId) }
            )
            CopyableLineLight(
                title = "To",
                value = tx.toUserId,
                accent = accent,
                textMain = textMain,
                textSub = textSub,
                onCopy = { onCopy("To UserId", tx.toUserId) }
            )

            tx.note?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Note: $it",
                    color = textSub,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CopyableLineLight(
    title: String,
    value: String,
    accent: Color,
    textMain: Color,
    textSub: Color,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onCopy),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title:",
            color = textSub,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = value.take(22) + if (value.length > 22) "…" else "",
            color = textMain,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onCopy) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = accent)
        }
    }
}
