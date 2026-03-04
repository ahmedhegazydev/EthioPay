package com.kbyai.idcardrecognition.payment.stripe

import CardNumberVisualTransformation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayWithScannedCard(
    scanned: ScannedCardUi?,
    onCreatePaymentIntent: suspend () -> String,
    onStatus: (String) -> Unit,
    stripe: Stripe,
    isPaying: Boolean,
    setIsPaying: (Boolean) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var expMonth by remember { mutableStateOf("") }
    var expYear by remember { mutableStateOf("") } // YY
    var cvc by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val activity = ctx.findActivity() // ✅ Activity مضمون

    val scope = rememberCoroutineScope()

    val cardBg = Color.White
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val border = Color(0xFFE5E7EB)
    val accent = Color(0xFF4F46E5)
    val accentSoft = Color(0xFFEEF2FF)
    val warn = Color(0xFFD97706)

    // ✅ Seed once per scanned number
    var seeded by remember(scanned?.number) { mutableStateOf(false) }

    LaunchedEffect(scanned?.number) {
        if (scanned != null && !seeded) {
            number = scanned.number.filter(Char::isDigit).take(19) // ✅ مش 16
            expMonth = (scanned.expMonth ?: 0).toString().padStart(2, '0')

            val y = scanned.expYear
            expYear = when {
                y == null -> ""
                y >= 2000 -> (y % 100).toString().padStart(2, '0')
                else -> y.toString().padStart(2, '0')
            }

            cvc = ""
            seeded = true // ✅ مهم جدًا
        }
    }

    // Validation
    val mm = expMonth.toIntOrNull()
    val yy2 = expYear.toIntOrNull()

    val clean = remember(number) { number.filter(Char::isDigit) }

    val isNumberOk = clean.length in 13..19 && isValidLuhn(clean)
    val isMonthOk = mm != null && mm in 1..12
    val isYearOk = yy2 != null && expYear.length == 2 && yy2 in 0..99
    val isCvcOk = cvc.length in 3..4

    val canPay = !isPaying && isNumberOk && isMonthOk && isYearOk && isCvcOk

    val hint = when {
        clean.isBlank() -> "Enter card number"
        !isNumberOk -> "Card number invalid"
        !isMonthOk -> "Month must be 01–12"
        !isYearOk -> "Year must be YY"
        !isCvcOk -> "CVC must be 3–4 digits"
        else -> "Ready to pay"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = accentSoft,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    Icon(Icons.Default.Payments, contentDescription = null, tint = accent)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text("Card Payment", color = textMain, fontWeight = FontWeight.Bold)
                    Text(
                        "Confirm securely with Stripe",
                        color = textSub,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isPaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = accent
                    )
                }
            }

            val chipColor = if (canPay || isPaying) accentSoft else Color(0xFFFFF7ED)
            val chipText = if (canPay || isPaying) accent else warn
            Surface(color = chipColor, shape = RoundedCornerShape(999.dp)) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (canPay || isPaying) Icons.Default.Lock else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = chipText
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(hint, color = chipText, style = MaterialTheme.typography.labelLarge)
                }
            }

            // Card Number (store digits only)
            OutlinedTextField(
                value = clean,
//                onValueChange = { input -> number = input.filter(Char::isDigit).take(19) },
                onValueChange = { input -> number = input.filter(Char::isDigit).take(16) },
                        label = { Text("Card Number") },
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = CardNumberVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = expMonth,
                    onValueChange = { expMonth = it.filter(Char::isDigit).take(2) },
                    label = { Text("MM") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = expYear,
                    onValueChange = { expYear = it.filter(Char::isDigit).take(2) }, // ✅ YY = 2
                    label = { Text("YY") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cvc,
                    onValueChange = { cvc = it.filter(Char::isDigit).take(4) },
                    label = { Text("CVC") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = canPay,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                onClick = {
                    val _mm = expMonth.toIntOrNull()
                    val _yyFull = yyToFullYear(expYear)

                    if (_mm == null || _yyFull == null) {
                        onStatus("Invalid expiry")
                        return@Button
                    }

                    val digits = clean
                    if (!isValidLuhn(digits)) {
                        onStatus("Card number invalid. Please retype.")
                        return@Button
                    }

                    scope.launch {
                        try {
                            setIsPaying(true)
                            onStatus("Creating PaymentIntent…")

                            val clientSecret = onCreatePaymentIntent()

                            val card = PaymentMethodCreateParams.Card.Builder()
                                .setNumber(digits)       // ✅ clean digits only
                                .setExpiryMonth(_mm)
                                .setExpiryYear(_yyFull)  // ✅ 2030
                                .setCvc(cvc)
                                .build()

                            val pmParams = PaymentMethodCreateParams.create(card)
                            val confirmParams =
                                ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                                    pmParams,
                                    clientSecret
                                )

                            onStatus("Confirming payment…")
                            activity?.let { stripe.confirmPayment(it, confirmParams) }
                        } catch (e: Exception) {
                            setIsPaying(false)
                            onStatus("Error: ${e.message}")
                        }
                    }
                }
            ) {
                if (isPaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Processing…", color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Pay Now", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                "Tip: Double-check expiry (MM/YY) and CVC (3–4 digits).",
                color = textSub,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun isValidLuhn(number: String): Boolean {
    if (number.length !in 13..19) return false
    var sum = 0
    var alt = false
    for (i in number.length - 1 downTo 0) {
        var n = number[i].digitToIntOrNull() ?: return false
        if (alt) { n *= 2; if (n > 9) n -= 9 }
        sum += n
        alt = !alt
    }
    return sum % 10 == 0
}

fun yyToFullYear(yyText: String): Int? {
    val yy = yyText.toIntOrNull() ?: return null
    if (yyText.length != 2) return null
    return 2000 + yy
}
