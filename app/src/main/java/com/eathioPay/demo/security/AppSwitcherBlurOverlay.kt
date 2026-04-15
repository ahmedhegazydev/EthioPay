package com.eathioPay.demo.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

@Composable
fun AppSwitcherBlurOverlay(show: Boolean) {
    if (!show) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(24.dp)              // blur
            .background(Color.Black.copy(alpha = 0.25f)) // dim (اختياري)
    )
}
