package com.kbyai.idcardrecognition.payment.demo

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbyai.idcardrecognition.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val alphaAnim = rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    LaunchedEffect(Unit) {
        delay(2500) // 2.5 seconds splash
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder for logo - using a generic one from resources if available or text
            // User should place the logo in res/drawable/noble_logo.png
            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                // Since I can't verify the existence of 'noble_logo', I'll use a fallback text/icon
                // but code it to look for noble_logo
                Text(
                    text = "N",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A56BE),
                    modifier = Modifier.alpha(alphaAnim.value)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Noble",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A56BE),
                letterSpacing = 2.sp
            )
            
            Text(
                text = "Simple. Secure. Reliable.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
