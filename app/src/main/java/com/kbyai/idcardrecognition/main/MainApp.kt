package com.kbyai.idcardrecognition.main

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.stripe.android.Stripe

@Composable
fun MainApp(
    stripe: Stripe,
    scannedCard: State<ScannedCardUi?>,
    onScanClick: () -> Unit,
    setLastTopupAmount: (Int) -> Unit,
    getRefreshSignal: () -> Int,
    bumpRefresh: () -> Unit,
    getIsPaying: () -> Boolean,
    setIsPaying: (Boolean) -> Unit,
    getSuccessDialog: () -> Boolean,
    setSuccessDialog: (Boolean) -> Unit,
    getErrorDialog: () -> String?,
    setErrorDialog: (String?) -> Unit,
) {
    var scanDebugDialog by rememberSaveable { mutableStateOf<String?>(null) }

    AppDialogs(
        success = getSuccessDialog(),
        error = getErrorDialog(),
        scanDebugText = scanDebugDialog,
        onDismissSuccess = { setSuccessDialog(false) },
        onDismissError = { setErrorDialog(null) },
        onDismissScanDebug = { scanDebugDialog = null }
    )

    AppNavGraph(
        stripe = stripe,
        scannedCard = scannedCard.value,
        onScanClick = onScanClick,
        setLastTopupAmount = setLastTopupAmount,
        getRefreshSignal = getRefreshSignal,
        bumpRefresh = bumpRefresh,
        getIsPaying = getIsPaying,
        setIsPaying = setIsPaying,
        setScanDebugDialog = { scanDebugDialog = it },
        setErrorDialog = setErrorDialog
    )
}
