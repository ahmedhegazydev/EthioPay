package com.eathioPay.demo.payment.demo

import androidx.compose.runtime.Composable
import com.eathioPay.demo.feature.home.presentation.HomeScreen
import com.eathioPay.demo.payment.models.ScannedCardUi
import com.stripe.android.Stripe

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
    HomeScreen(
        userId = userId,
        scannedCard = scannedCard,
        onScanClick = onScanClick,
        onLogOutClicked = onLogOutClicked,
        stripe = stripe,
        isPaying = isPaying,
        setIsPaying = setIsPaying,
        refreshSignal = refreshSignal,
        bumpRefresh = bumpRefresh,
    )
}
