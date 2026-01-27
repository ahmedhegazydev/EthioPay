import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ethiopay.payment.models.MaskedCard
import com.example.ethiopay.payment.models.ScannedCardUi
import com.example.ethiopay.payment.models.StripeIntentResponse
import com.example.ethiopay.payment.stripe.PayWithScannedCard
import com.example.ethiopay.payment.stripe.fetchClientSecret
import com.example.ethiopay.payment.stripe.postJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.stripe.android.paymentsheet.PaymentSheet
import io.scanbot.sdk.creditcard.entity.CreditCard
import io.scanbot.sdk.ui_v2.creditcard.CreditCardScannerActivity

@Composable
fun HomeScreenStripe(
    paymentSheet: PaymentSheet,
    baseConfig: PaymentSheet.Configuration,
    scannedCard: ScannedCardUi?,
    onScanClick: () -> Unit,
    onLogOutClicked: () -> Unit,
    isPaying: Boolean,
    setIsPaying: (Boolean) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Idle") }

    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .padding(all = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            fontSize = 25.sp,
            text = status,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold // = 700
        )


        Spacer(modifier = Modifier.height(12.dp))
        // Pay
//        Button(
//            enabled = !isLoading,
//            onClick = {
//                scope.launch {
//                    try {
//                        isLoading = true
//                        status = "Creating PaymentIntent..."
//
//                        val respStr = postJson(
//                            "https://stripe-backend-smjf.onrender.com/create-payment-intent",
//                            """{"amount":1000,"currency":"usd"}"""
//                        )
//
//                        val obj = org.json.JSONObject(respStr)
//                        if (obj.has("error")) {
//                            status = "Backend error: ${obj.getString("error")}"
//                            return@launch
//                        }
//
//                        val clientSecret = obj.getString("clientSecret")
//                        val customerId = obj.getString("customerId")
//                        val eKey = obj.getString("ephemeralKey")
//
//                        val customerConfig = PaymentSheet.CustomerConfiguration(
//                            id = customerId,
//                            ephemeralKeySecret = eKey
//                        )
//
//
//                        val config2 = PaymentSheet.Configuration(
//                            merchantDisplayName = "Ethiopay",
//                            customer = customerConfig,
//                            allowsDelayedPaymentMethods = true
//                        )
//
//                        status = "Opening PaymentSheet..."
//
//                        paymentSheet.presentWithPaymentIntent(clientSecret, config2)
//                    } catch (e: Exception) {
//                        status = "Error: ${e.message}"
//                    } finally {
//                        isLoading = false
//                    }
//                }
//            }
//        ) {
//            Text(if (isLoading) "Loading..." else "Pay with Stripe")
//        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scan
        OutlinedButton(
            onClick = onScanClick, modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "Scan Card",
                fontSize = 30.sp, modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LogOut
        OutlinedButton(
            onClick = onLogOutClicked, modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "LogOut",
                fontSize = 30.sp, modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

//        Spacer(modifier = Modifier.height(12.dp))

        // Save Card (SetupIntent)
//        OutlinedButton(
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !isLoading,
//            onClick = {
//                scope.launch {
//                    try {
//                        isLoading = true
//                        status = "Creating SetupIntent..."
//
//                        val respStr = postJson(
//                            "https://stripe-backend-smjf.onrender.com/create-setup-intent",
//                            "{}"
//                        )
//                        val resp = com.google.gson.Gson()
//                            .fromJson(respStr, StripeIntentResponse::class.java)
//
//                        val customerConfig = PaymentSheet.CustomerConfiguration(
//                            id = resp.customerId,
//                            ephemeralKeySecret = resp.ephemeralKey
//                        )
//
//                        val config2 = PaymentSheet.Configuration(
//                            merchantDisplayName = "Ethiopay",
//                            customer = customerConfig,
//                            allowsDelayedPaymentMethods = true
//                        )
//
//                        status = "Opening Setup Sheet..."
//                        paymentSheet.presentWithSetupIntent(resp.clientSecret, config2)
//                    } catch (e: Exception) {
//                        status = "Error: ${e.message}"
//                    } finally {
//                        isLoading = false
//                    }
//                }
//            }
//        ) {
//            Text(
//                "Save Card",
//                fontSize = 30.sp,
//                modifier = Modifier.align(Alignment.CenterVertically)
//            )
//        }

        Spacer(modifier = Modifier.height(12.dp))


        // Masked display تحت الأزرار
        if (scannedCard != null) {

            PayWithScannedCard(
                scanned = scannedCard,
                publishableKey = "pk_test_51RbPA2FYLQId6JpF6JCXr9IbaBBPOiCmz2h1Xi4eEzwflSlarggnks3OAPrExquHolTmNgvKI4NbSSigUYHRBBJw00wmWUf0xk",
                onCreatePaymentIntent = {
                    val respStr = postJson(
                        "https://stripe-backend-smjf.onrender.com/create-payment-intent",
                        """{"amount":1000,"currency":"usd"}"""
                    )
                    val obj = org.json.JSONObject(respStr)
                    obj.getString("clientSecret")
                },
                onStatus = { status = it },
                isPaying = isPaying,
                setIsPaying = setIsPaying
            )

        }
    }
}
