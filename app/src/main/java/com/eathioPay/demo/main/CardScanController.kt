package com.eathioPay.demo.main

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.eathioPay.demo.payment.models.ScannedCardUi
import com.eathioPay.demo.payment.stripe.parseExpiry
import io.scanbot.sdk.creditcard.entity.CreditCard
import io.scanbot.sdk.ui_v2.creditcard.CreditCardScannerActivity
import io.scanbot.sdk.ui_v2.creditcard.configuration.CreditCardScannerScreenConfiguration

class CardScanController(
    val scannedCard: State<ScannedCardUi?>,
    val startScan: () -> Unit
)

@Composable
fun rememberCardScanController(): CardScanController {
    val ctx = LocalContext.current
    var scannedCard by remember { mutableStateOf<ScannedCardUi?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = CreditCardScannerActivity.ResultContract()
    ) { resultEntity ->
        if (!resultEntity.resultOk) {
            Toast.makeText(ctx, "Scan canceled/failed", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val raw = resultEntity.result?.creditCard ?: run {
            Toast.makeText(ctx, "No card returned", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val cc = CreditCard(raw)
        val number = cc.cardNumber.value.text.filter { it.isDigit() }
        val expText = cc.expiryDate?.value?.text
        val (m, y) = parseExpiry(expText)

        scannedCard = ScannedCardUi(
            number = number,
            expMonth = m,
            expYear = y,
            holder = cc.cardholderName?.value?.text
        )
    }

    val startScan = {
        val configuration = CreditCardScannerScreenConfiguration()
        launcher.launch(configuration)
    }

    return remember { CardScanController(scannedCard = derivedStateOf { scannedCard }, startScan = startScan) }
}
