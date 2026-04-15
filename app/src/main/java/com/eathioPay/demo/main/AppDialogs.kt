package com.eathioPay.demo.main

import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString

@Composable
fun AppDialogs(
    success: Boolean,
    error: String?,
    scanDebugText: String?,
    onDismissSuccess: () -> Unit,
    onDismissError: () -> Unit,
    onDismissScanDebug: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val ctx = LocalContext.current

    if (scanDebugText != null) {
        AlertDialog(
            onDismissRequest = onDismissScanDebug,
            title = { Text("Scan Result") },
            text = { Text(scanDebugText) },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(scanDebugText))
                    Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
                }) { Text("Copy") }
            },
            dismissButton = {
                TextButton(onClick = onDismissScanDebug) { Text("Close") }
            }
        )
    }

    if (success) {
        AlertDialog(
            onDismissRequest = onDismissSuccess,
            title = { Text("Payment Successful 🎉") },
            text = { Text("Your payment has been completed successfully.") },
            confirmButton = { Button(onClick = onDismissSuccess) { Text("OK") } }
        )
    }

    if (error != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Payment Failed ❌") },
            text = { Text(error) },
            confirmButton = { Button(onClick = onDismissError) { Text("OK") } }
        )
    }
}
