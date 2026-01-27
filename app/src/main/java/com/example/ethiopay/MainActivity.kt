package com.example.ethiopay

import HomeScreenStripe
import HomeScreenStripe2
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ethiopay.login.FirebasePhoneAuthScreen
import com.example.ethiopay.payment.models.MaskedCard
import com.example.ethiopay.payment.models.ScannedCardUi
import com.example.ethiopay.payment.models.Screen
import com.example.ethiopay.payment.stripe.detectBrand
import com.example.ethiopay.payment.stripe.fetchSetupIntentClientSecret
import com.example.ethiopay.payment.stripe.parseExpiry
import com.example.ethiopay.payment.stripe.postJson
import com.google.firebase.auth.FirebaseAuth
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.scanbot.sdk.creditcard.entity.CreditCard
import io.scanbot.sdk.ui_v2.creditcard.CreditCardScannerActivity
import io.scanbot.sdk.ui_v2.creditcard.configuration.CreditCardScannerScreenConfiguration
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : FragmentActivity() {
    private lateinit var stripe: Stripe

    lateinit var paymentSheet: PaymentSheet
    lateinit var paymentSheetConfig: PaymentSheet.Configuration
    private var scannedCard by mutableStateOf<ScannedCardUi?>(null)
    private var isPaying by mutableStateOf(false)

    private var payFinishHandler: ((Boolean, String?) -> Unit)? = null

    private fun startSaveCardFlow() {
        lifecycleScope.launch {
            try {
                val clientSecret = fetchSetupIntentClientSecret()
                paymentSheet.presentWithSetupIntent(
                    clientSecret,
                    paymentSheetConfig
                )
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }

//    private val resultLauncher: ActivityResultLauncher<CreditCardScannerScreenConfiguration> =
//        registerForActivityResult(CreditCardScannerActivity.ResultContract()) { resultEntity: CreditCardScannerActivity.Result ->
//            if (resultEntity.resultOk) {
//                resultEntity.result?.creditCard?.let {
//                    val creditCard = CreditCard(it)
//                    val cardNumber: String = creditCard.cardNumber.value.text
//                    val cardholderName: String = creditCard.cardholderName?.value?.text ?: ""
//                    val expiryDate: String? = creditCard.expiryDate?.value?.text
//                    Toast.makeText(
//                        this,
//                        "Card Number: $cardNumber, Cardholder Name: $cardholderName, Expiry Date: $expiryDate",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//                resultEntity.result?.creditCard?.let { raw ->
//                    val cc = CreditCard(raw)
//
//                    val number = cc.cardNumber.value.text.filter { it.isDigit() }
//                    val last4 = number.takeLast(4).ifEmpty { "----" }
//
//                    val holder = cc.cardholderName?.value?.text
//                    val exp = cc.expiryDate?.value?.text
////                    val brand = cc.cardType?.name // لو موجود في Scanbot عندك
//                    val brand = detectBrand(number)
//
//                    scannedCard = ScannedCardUi(
//                        brand = brand,
//                        last4 = last4,
//                        holder = holder,
//                        exp = exp
//                    )
//                }
//
//            }
//        }

    private val resultLauncher =
        registerForActivityResult(CreditCardScannerActivity.ResultContract()) { resultEntity ->
            Log.d("SCANBOT", "resultOk=${resultEntity.resultOk} result=${resultEntity.result}")

            if (!resultEntity.resultOk) {
                // هنا غالبًا Cancel أو Error أو Permission/License
                Toast.makeText(this, "Scan canceled/failed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val raw = resultEntity.result?.creditCard ?: run {
                Toast.makeText(this, "No card returned", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val cc = CreditCard(raw)
            val number = cc.cardNumber.value.text.filter { it.isDigit() }
            val last4 = number.takeLast(4).ifEmpty { "----" }
            val expText = cc.expiryDate?.value?.text // غالبًا "MM/YY" أو "MM/YYYY"
            val (m, y) = parseExpiry(expText)

//            scannedCard = ScannedCardUi(
//                brand = detectBrand(number),
//                last4 = last4,
//                holder = cc.cardholderName?.value?.text,
//                exp = cc.expiryDate?.value?.text
//            )
            scannedCard = ScannedCardUi(
                number = number,
                expMonth = m,
                expYear = y,
                holder = cc.cardholderName?.value?.text
            )

            // ✅ افتح PaymentSheet فورًا بعد الاسكان
//            openPaymentSheet(amount = 1000)
        }


    private fun openPaymentSheet(amount: Int = 1000) {
        lifecycleScope.launch {
            try {
                val respStr = postJson(
                    "https://stripe-backend-smjf.onrender.com/create-payment-intent",
                    """{"amount":$amount,"currency":"usd"}"""
                )

                val obj = org.json.JSONObject(respStr)
                val clientSecret = obj.getString("clientSecret")
                val customerId = obj.getString("customerId")
                val eKey = obj.getString("ephemeralKey")

                val customerConfig = PaymentSheet.CustomerConfiguration(
                    id = customerId,
                    ephemeralKeySecret = eKey
                )

                val config2 = PaymentSheet.Configuration(
                    merchantDisplayName = "Ethiopay",
                    customer = customerConfig,
                    allowsDelayedPaymentMethods = true
                )

                paymentSheet.presentWithPaymentIntent(clientSecret, config2)

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCardScan() {
        val configuration = CreditCardScannerScreenConfiguration()
        resultLauncher.launch(configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stripe = Stripe(
            context = this,
            publishableKey = "pk_test_51RbPA2FYLQId6JpF6JCXr9IbaBBPOiCmz2h1Xi4eEzwflSlarggnks3OAPrExquHolTmNgvKI4NbSSigUYHRBBJw00wmWUf0xk"
        )

        // ✅ create PaymentSheet BEFORE setContent
        paymentSheet = PaymentSheet(this) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> Toast.makeText(
                    this,
                    "✅ Payment Success",
                    Toast.LENGTH_LONG
                ).show()

                is PaymentSheetResult.Canceled -> Toast.makeText(
                    this,
                    "⚠️ Canceled",
                    Toast.LENGTH_LONG
                ).show()

                is PaymentSheetResult.Failed -> Toast.makeText(
                    this,
                    "❌ Failed: ${result.error.message}",
                    Toast.LENGTH_LONG
                ).show()

            }
        }

        paymentSheetConfig = PaymentSheet.Configuration(
            merchantDisplayName = "Ethiopay"
        )


        setContent {
            val navController = rememberNavController()
            val start = remember {
                if (FirebaseAuth.getInstance().currentUser != null) Screen.Home.route
                else Screen.Login.route
            }

            val success = showSuccessDialog
            val error = showErrorDialog

            // 👇 Success Dialog
            if (success) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    title = { Text("Payment Successful 🎉") },
                    text = { Text("Your payment has been completed successfully.") },
                    confirmButton = {
                        Button(onClick = { showSuccessDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            // 👇 Error Dialog
            if (error != null) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = null },
                    title = { Text("Payment Failed ❌") },
                    text = { Text(error) },
                    confirmButton = {
                        Button(onClick = { showErrorDialog = null }) {
                            Text("OK")
                        }
                    }
                )
            }


            NavHost(
                navController = navController,
//                startDestination = start
//                startDestination = Screen.Login.route
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Login.route) {
                    FirebasePhoneAuthScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
//                    HomeScreen(
////                        onLogout = {
////                            FirebaseAuth.getInstance().signOut()
////                            navController.navigate(Screen.Login.route) {
////                                popUpTo(Screen.Home.route) { inclusive = true }
////                            }
////                        }
//                    )
//                    HomeScreenStripe(
//                        paymentSheet = paymentSheet,
//                        config = paymentSheetConfig,
//                        scannedCard = scannedCard,
//                        onScanClick = { startCardScan() },
//                        onSaveClick = { startSaveCardFlow() }
//                    )
                    HomeScreenStripe(
                        paymentSheet = paymentSheet,
                        baseConfig = paymentSheetConfig,
                        scannedCard = scannedCard,
                        onScanClick = {
                            startCardScan()
                        },
                        onLogOutClicked = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        isPaying = isPaying,
                        setIsPaying = { isPaying = it }
                    )
//                    HomeScreenStripe2(
//                        scannedCard = scannedCard,
//                        onPayWithScannedCard = { cvc ->
//                            val s = scannedCard
//                            if (s == null) {
//                                Toast.makeText(
//                                    this@MainActivity,
//                                    "Scan card first",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                return@HomeScreenStripe2
//                            }
//
//                            val mm = s.expMonth
//                            val yy = s.expYear
//                            if (mm == null || yy == null) {
//                                Toast.makeText(
//                                    this@MainActivity,
//                                    "Missing expiry date",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                return@HomeScreenStripe
//                            }
//
//                            payWithScannedCard(
//                                number = s.number,
//                                expMonth = mm,
//                                expYear = yy,
//                                cvc = cvc
//                            )
//                        }
//                    )

                }
            }
        }
    }

    private var showSuccessDialog by mutableStateOf(false)
    private var showErrorDialog by mutableStateOf<String?>(null)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        stripe.onPaymentResult(
            requestCode, data,
            object : ApiResultCallback<PaymentIntentResult> {

                override fun onSuccess(result: PaymentIntentResult) {
                    val intent = result.intent
                    val status = intent.status
                    val msg = intent.lastPaymentError?.message

                    if (status == StripeIntent.Status.Succeeded) {
                        Toast.makeText(this@MainActivity, "Payment successful 🎉", Toast.LENGTH_LONG).show()
                        showSuccessDialog = true
                        payFinishHandler?.invoke(true, null)
                    } else {
                        val err = msg ?: "Payment failed: $status"
                        Toast.makeText(this@MainActivity, err, Toast.LENGTH_LONG).show()
                        showErrorDialog = err
                        payFinishHandler?.invoke(false, err)
                    }
                }

                override fun onError(e: Exception) {
                    val err = e.message ?: "Payment error"
                    Toast.makeText(this@MainActivity, err, Toast.LENGTH_LONG).show()
                    showErrorDialog = err
                    payFinishHandler?.invoke(false, err)
                }
            }
        )
    }



    private fun payWithScannedCard(
        number: String,
        expMonth: Int,
        expYear: Int,
        cvc: String
    ) {
        lifecycleScope.launch {
            try {
                // 1) call backend to create PI
                val respStr = postJson(
                    "https://stripe-backend-smjf.onrender.com/create-payment-intent",
                    """{"amount":1000,"currency":"usd"}"""
                )

                val obj = JSONObject(respStr)
                if (obj.has("error")) {
                    Toast.makeText(
                        this@MainActivity,
                        "Backend error: ${obj.getString("error")}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val clientSecret = obj.getString("clientSecret")

                // 1) Card params via Builder (عشان constructor internal)
                val card = PaymentMethodCreateParams.Card.Builder()
                    .setNumber(number)
                    .setExpiryMonth(expMonth)
                    .setExpiryYear(expYear)
                    .setCvc(cvc)
                    .build()

// 2) PaymentMethodCreateParams
                val pmParams = PaymentMethodCreateParams.create(card)

// 3) Confirm PI
                val confirmParams =
                    ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                        pmParams,
                        clientSecret
                    )

                stripe.confirmPayment(this@MainActivity, confirmParams)

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
