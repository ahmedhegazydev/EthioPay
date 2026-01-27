package com.example.ethiopay.payment.stripe

import CardNumberVisualTransformation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.ethiopay.payment.models.ScannedCardUi
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.coroutines.launch

@Composable
fun PayWithScannedCard(
    scanned: ScannedCardUi?,
    onCreatePaymentIntent: suspend () -> String,
    onStatus: (String) -> Unit,
    stripe: Stripe,                 // ✅ pass from Activity
    activity: FragmentActivity,      // ✅ pass (MainActivity)
    publishableKey: String,
    isPaying: Boolean,
    setIsPaying: (Boolean) -> Unit
) {
    val context = LocalContext.current
//    val activity = context as FragmentActivity

    var number by remember { mutableStateOf("") }
    var expMonth by remember { mutableStateOf("") }
    var expYear by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
//    var isPaying by remember { mutableStateOf(false) }

    // ✅ handler بيتنادى من Activity لما Stripe يخلص
    val finishHandler: (Boolean, String?) -> Unit = { success, msg ->
        setIsPaying(false)
        if (success) {
            onStatus("Payment successful")
        } else {
            onStatus(msg ?: "Payment failed")
        }
    }

//    // ✅ سجل الـhandler مرة واحدة
//    LaunchedEffect(Unit) {
//        onRegisterFinishHandler(finishHandler)
//    }

//    val stripe = remember(publishableKey) { Stripe(context, publishableKey) }

    LaunchedEffect(scanned) {
        if (scanned != null) {
            number = scanned.number.filter(Char::isDigit).take(16)
            expMonth = scanned.expMonth?.toString().orEmpty()
            expYear = scanned.expYear?.toString().orEmpty()
            cvc = ""
        }
    }

    Column {

        OutlinedTextField(
            value = number,
            onValueChange = { input -> number = input.filter(Char::isDigit).take(16) },
            label = { Text("Card Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = CardNumberVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = expMonth,
            onValueChange = { expMonth = it.filter(Char::isDigit).take(2) },
            label = { Text("Exp Month (MM)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = expYear,
            onValueChange = { expYear = it.filter(Char::isDigit).take(4) },
            label = { Text("Exp Year (YYYY)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cvc,
            onValueChange = { cvc = it.filter(Char::isDigit).take(4) },
            label = { Text("CVC") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPaying,
            onClick = {
                val mm = expMonth.toIntOrNull()
                val yy = expYear.toIntOrNull()

                if (number.length < 12 || mm == null || yy == null || cvc.length < 3) {
                    onStatus("Invalid card data")
                    return@Button
                }

                scope.launch {
                    try {
                        setIsPaying(true)
                        onStatus("Creating PaymentIntent...")

                        val clientSecret = onCreatePaymentIntent()

                        val card = PaymentMethodCreateParams.Card.Builder()
                            .setNumber(number)
                            .setExpiryMonth(mm)
                            .setExpiryYear(yy)
                            .setCvc(cvc)
                            .build()

                        val pmParams = PaymentMethodCreateParams.create(card)

                        val confirmParams =
                            ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                                pmParams,
                                clientSecret
                            )

                        onStatus("Confirming payment...")
                        stripe.confirmPayment(activity, confirmParams)

                    } catch (e: Exception) {
//                        isPaying = false
                        setIsPaying(false)
                        onStatus("Error: ${e.message}")
                    }
                }
            }
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (isPaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Pay Now", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

