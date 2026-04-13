package com.kbyai.idcardrecognition.payment.demo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch

@Composable
fun TransferTab(
    userId: String,
    onStatus: (String) -> Unit,
    bumpRefresh: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val scroll = rememberScrollState()

    var toUserId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val nobleBlue = Color(0xFF1A56BE)
    val textMain = Color(0xFF1F2937)
    val textSub = Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Send Money",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textMain
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Recipient ID
            Text("Recipient ID", fontWeight = FontWeight.SemiBold, color = textSub)
            OutlinedTextField(
                value = toUserId,
                onValueChange = { toUserId = it.trim() },
                placeholder = { Text("Enter account number or ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = nobleBlue) },
                singleLine = true
            )

            // Amount
            Text("Amount (ETB)", fontWeight = FontWeight.SemiBold, color = textSub)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter(Char::isDigit) },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                singleLine = true
            )

            // Note (Optional)
            Text("Note (Optional)", fontWeight = FontWeight.SemiBold, color = textSub)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("What's this for?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                val amt = amount.toIntOrNull() ?: 0
                if (amt <= 0 || toUserId.isBlank()) {
                    Toast.makeText(ctx, "Please enter valid details", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                scope.launch {
                    try {
                        isLoading = true
                        onStatus("Sending...")
                        DemoApi.transfer(userId, toUserId, amt, note.ifBlank { "Transfer" })
                        bumpRefresh()
                        Toast.makeText(ctx, "Sent Successfully ✅", Toast.LENGTH_LONG).show()
                        toUserId = ""
                        amount = ""
                        note = ""
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                        onStatus("Ready")
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = nobleBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Send Now", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Text(
            "Payments are secure and encrypted.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = textSub,
            fontSize = 12.sp
        )
    }
}
