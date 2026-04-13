package com.kbyai.idcardrecognition.payment.demo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var items by remember { mutableStateOf<List<TxDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val nobleBlue = Color(0xFF1A56BE)
    val textMain = Color(0xFF1F2937)
    val textSub = Color(0xFF6B7280)

    fun refresh() {
        scope.launch {
            try {
                isLoading = true
                onStatus("Updating...")
                items = DemoApi.getTransactions(userId)
                onStatus("Ready")
            } catch (e: Exception) {
                onStatus("Error updating")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId, refreshSignal) { refresh() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Activity",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textMain
            )
            IconButton(onClick = { refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = nobleBlue)
            }
        }

        if (isLoading && items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = nobleBlue)
            }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found", color = textSub)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items) { tx ->
                    TransactionItem(tx, nobleBlue, textMain, textSub)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: TxDto, nobleBlue: Color, textMain: Color, textSub: Color) {
    val isIncoming = tx.toUserId == tx.fromUserId || tx.fromUserId.startsWith("system") || tx.note?.contains("topup", true) == true
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isIncoming) Color(0xFFE8F5E9) else Color(0xFFFCE4EC), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncoming) Color(0xFF2E7D32) else Color(0xFFC2185B)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isIncoming) "Money Received" else "Money Sent",
                    fontWeight = FontWeight.Bold,
                    color = textMain,
                    fontSize = 16.sp
                )
                Text(
                    text = tx.createdAt.take(16),
                    color = textSub,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "${if (isIncoming) "+" else "-"}${tx.amount} ${tx.currency}",
                fontWeight = FontWeight.ExtraBold,
                color = if (isIncoming) Color(0xFF2E7D32) else textMain,
                fontSize = 18.sp
            )
        }
    }
}
