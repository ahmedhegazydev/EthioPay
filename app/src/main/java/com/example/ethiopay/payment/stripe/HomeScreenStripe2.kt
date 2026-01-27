import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.ethiopay.payment.models.MaskedCard
import com.example.ethiopay.payment.models.ScannedCardUi
import com.example.ethiopay.payment.models.StripeIntentResponse
import com.example.ethiopay.payment.stripe.fetchClientSecret
import com.example.ethiopay.payment.stripe.postJson
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.stripe.android.paymentsheet.PaymentSheet
import io.scanbot.sdk.creditcard.entity.CreditCard
import io.scanbot.sdk.ui_v2.creditcard.CreditCardScannerActivity
import org.json.JSONObject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun HomeScreenStripe2(
    scannedCard: ScannedCardUi?,
    onPayWithScannedCard: (cvc: String) -> Unit
) {
    var cvc by remember { mutableStateOf("") }

    Column {
        // عرض بيانات الاسكان (غير حساسة)
        if (scannedCard != null) {
            Text("Scanned ✅ •••• ${scannedCard.number.takeLast(4)}")
            scannedCard.expMonth?.let { m ->
                scannedCard.expYear?.let { y ->
                    Text("Expiry: $m/$y")
                }
            }
        } else {
            Text("Scan card first")
        }

        OutlinedTextField(
            value = cvc,
            onValueChange = { cvc = it.filter(Char::isDigit).take(4) },
            label = { Text("CVC") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )

        Button(
            enabled = scannedCard != null && cvc.length >= 3,
            onClick = { onPayWithScannedCard(cvc) }
        ) {
            Text("Pay with scanned card")
        }
    }
}
