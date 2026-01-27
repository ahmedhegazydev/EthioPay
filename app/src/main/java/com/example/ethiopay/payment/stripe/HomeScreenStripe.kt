import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.ethiopay.payment.models.ScannedCardUi
import com.example.ethiopay.payment.stripe.fetchClientSecret
import com.stripe.android.paymentsheet.PaymentSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreenStripe(
    paymentSheet: PaymentSheet,
    config: PaymentSheet.Configuration,
    scannedCard: ScannedCardUi?,
    onScanClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("Idle") }
    var isLoading by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<String?>(null) }



    Column {
        Button(
            enabled = !isLoading,
            onClick = {
//                scope.launch {
//                    isLoading = true
//                    status = "Creating PaymentIntent..."
//                    val clientSecret = runCatching {
//                        createPaymentIntent(
//                            baseUrl = "https://stripe-backend-smjf.onrender.com",
//                            amount = 1000, // 1000 = 10.00 لو العملة فيها 2 decimals (زي USD)
//                            currency = "usd"
//                        )
//                    }.getOrNull()
//                    if (clientSecret == null) {
//                        status = "⚠️ Failed to get clientSecret"
//                        isLoading = false
//                        return@launch
//                    }
//
//                    status = "Presenting PaymentSheet..."
//                    paymentSheet.presentWithPaymentIntent(
//                        clientSecret,
//                        config
//                    )
//
//                    isLoading = false
//                }
                scope.launch {
                    isLoading = true
                    lastResult = null
                    try {
                        val clientSecret = withContext(Dispatchers.IO) { fetchClientSecret() }
                        paymentSheet.presentWithPaymentIntent(clientSecret, config)
                    } catch (e: Exception) {
                        lastResult = "Failed to start payment: ${e.message}"
                        Log.e("Stripe", "start payment error", e)
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            Text(if (isLoading) "Loading..." else "Pay with Stripe")
        }

        lastResult?.let { Text(it) }
        Text(status)

        Spacer(modifier = Modifier.width(12.dp))

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedButton(onClick = onScanClick) { Text("Scan Card") }

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedButton(onClick = onSaveClick) { Text("Save Card") }

        scannedCard?.let {
            Text("Card: ${it.brand ?: "Card"} •••• ${it.last4}")
            if (!it.holder.isNullOrBlank()) Text("Name: ${it.holder}")
            if (!it.exp.isNullOrBlank()) Text("Exp: ${it.exp}")
        }

    }
}

