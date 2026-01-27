package com.example.ethiopay

import HomeScreenStripe
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.firebase.auth.FirebaseAuth
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.scanbot.sdk.creditcard.entity.CreditCard
import io.scanbot.sdk.ui_v2.creditcard.CreditCardScannerActivity
import io.scanbot.sdk.ui_v2.creditcard.configuration.CreditCardScannerScreenConfiguration
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    lateinit var paymentSheet: PaymentSheet
    lateinit var paymentSheetConfig: PaymentSheet.Configuration
    private var scannedCard by mutableStateOf<ScannedCardUi?>(null)


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

    private val resultLauncher: ActivityResultLauncher<CreditCardScannerScreenConfiguration> =
        registerForActivityResult(CreditCardScannerActivity.ResultContract()) { resultEntity: CreditCardScannerActivity.Result ->
            if (resultEntity.resultOk) {
                resultEntity.result?.creditCard?.let {
                    val creditCard = CreditCard(it)
                    val cardNumber: String = creditCard.cardNumber.value.text
                    val cardholderName: String = creditCard.cardholderName?.value?.text ?: ""
                    val expiryDate: String? = creditCard.expiryDate?.value?.text
                    Toast.makeText(
                        this,
                        "Card Number: $cardNumber, Cardholder Name: $cardholderName, Expiry Date: $expiryDate",
                        Toast.LENGTH_LONG
                    ).show()
                }
                resultEntity.result?.creditCard?.let { raw ->
                    val cc = CreditCard(raw)

                    val number = cc.cardNumber.value.text.filter { it.isDigit() }
                    val last4 = number.takeLast(4).ifEmpty { "----" }

                    val holder = cc.cardholderName?.value?.text
                    val exp = cc.expiryDate?.value?.text
//                    val brand = cc.cardType?.name // لو موجود في Scanbot عندك
                    val brand = detectBrand(number)

                    scannedCard = ScannedCardUi(
                        brand = brand,
                        last4 = last4,
                        holder = holder,
                        exp = exp
                    )
                }

            }
        }


    private fun startCardScan() {
        val configuration = CreditCardScannerScreenConfiguration()
        resultLauncher.launch(configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ create PaymentSheet BEFORE setContent
        paymentSheet = PaymentSheet(this) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> Toast.makeText(this, "✅ Payment Success", Toast.LENGTH_LONG).show()
                is PaymentSheetResult.Canceled -> Toast.makeText(this, "⚠️ Canceled", Toast.LENGTH_LONG).show()
                is PaymentSheetResult.Failed -> Toast.makeText(this, "❌ Failed: ${result.error.message}", Toast.LENGTH_LONG).show()

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
                    HomeScreenStripe(
                        paymentSheet = paymentSheet,
                        config = paymentSheetConfig,
                        scannedCard = scannedCard,
                        onScanClick = { startCardScan() },
                        onSaveClick = { startSaveCardFlow() }
                    )

                }
            }
        }
    }
}
