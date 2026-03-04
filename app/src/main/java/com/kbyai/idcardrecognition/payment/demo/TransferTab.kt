package com.kbyai.idcardrecognition.payment.demo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch

@Composable
fun TransferTab(
    userId: String,
    onStatus: (String) -> Unit,
    bumpRefresh: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var toUserId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("5000") }
    var note by remember { mutableStateOf("demo") }
    var isLoading by remember { mutableStateOf(false) }

    val amtInt = amount.toIntOrNull() ?: 0
    val canSend = toUserId.isNotBlank() && amtInt > 0 && !isLoading

    // 🎨 Light palette
    val pageBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7F8FC), Color(0xFFF2F4FA), Color(0xFFF7F8FC))
    )
    val cardBg = Color.White
    val subtle = Color(0xFF6B7280)
    val border = Color(0xFFE5E7EB)
    val accent = Color(0xFF4F46E5)   // Indigo
    val accentSoft = Color(0xFFEEF2FF)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(pageBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(accentSoft, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = accent
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Transfer",
                            color = Color(0xFF111827),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    clipboard.setText(AnnotatedString("Transfer"))
                                    Toast.makeText(ctx, "Copied ✅", Toast.LENGTH_SHORT).show()
                                }
                            )
                        )
                        Text(
                            "Send money instantly to another user",
                            color = subtle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(userId))
                            Toast.makeText(ctx, "Your UserId copied ✅", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy userId",
                            tint = accent
                        )
                    }
                }

                Surface(
                    color = accentSoft,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "From: ${userId.take(12)}…",
                        color = Color(0xFF374151),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Form
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = toUserId,
                    onValueChange = { toUserId = it.trim() },
                    label = { Text("To UserId") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = accent
                        )
                    },
                    trailingIcon = {
                        if (toUserId.isNotBlank()) {
                            IconButton(onClick = {
                                clipboard.setText(AnnotatedString(toUserId))
                                Toast.makeText(ctx, "To UserId copied ✅", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = accent
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent,
                        unfocusedBorderColor = border
                    )
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit).take(9) },
                    label = { Text("Amount") },
                    supportingText = { Text("Integer value (e.g., 5000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent,
                        unfocusedBorderColor = border
                    )
                )

                // Quick amounts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAmountChipLight("500", accentSoft, accent) { amount = "500" }
                    QuickAmountChipLight("1000", accentSoft, accent) { amount = "1000" }
                    QuickAmountChipLight("5000", accentSoft, accent) { amount = "5000" }
                    QuickAmountChipLight("10000", accentSoft, accent) { amount = "10000" }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent,
                        unfocusedBorderColor = border
                    )
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = canSend,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    onClick = {
                        val amt = amount.toIntOrNull() ?: return@Button
                        scope.launch {
                            try {
                                isLoading = true
                                onStatus("Sending transfer…")

                                DemoApi.transfer(userId, toUserId, amt, note)

                                bumpRefresh()
                                Toast.makeText(ctx, "Transfer SUCCESS ✅", Toast.LENGTH_SHORT).show()
                                onStatus("Transfer SUCCESS ✅")
                            } catch (e: Exception) {
                                onStatus("Transfer error: ${e.message}")
                                Toast.makeText(ctx, "Transfer failed ❌", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Sending…", fontWeight = FontWeight.SemiBold, color = Color.White)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text("Send Transfer", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
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

        // Hint card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
//                color = border
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Tip: Long-press the title to copy “Transfer”. Use the copy icon to share your UserId quickly.",
                color = subtle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun QuickAmountChipLight(
    label: String,
    bg: Color,
    fg: Color,
    onPick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onPick,
        label = { Text(label, color = fg, fontWeight = FontWeight.SemiBold) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = bg,
            selectedContainerColor = bg
        ),
        border = null,
        shape = RoundedCornerShape(999.dp)
    )
}
