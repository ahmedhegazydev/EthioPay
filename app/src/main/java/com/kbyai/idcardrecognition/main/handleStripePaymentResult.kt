package com.kbyai.idcardrecognition.main

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.kbyai.idcardrecognition.noble.data.NobleRepository
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.StripeIntent
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

fun handleStripePaymentResult(
    activity: MainActivity,
    stripe: Stripe,
    requestCode: Int,
    data: Intent?,
    getLastTopupAmount: () -> Int,
    setIsPaying: (Boolean) -> Unit,
    showSuccess: () -> Unit,
    showError: (String) -> Unit,
    bumpRefresh: () -> Unit,
) {
    stripe.onPaymentResult(
        requestCode,
        data,
        object : ApiResultCallback<PaymentIntentResult> {

            override fun onSuccess(result: PaymentIntentResult) {
                setIsPaying(false)

                val intent = result.intent
                val status = intent.status
                val err = intent.lastPaymentError?.message

                when (status) {
                    StripeIntent.Status.Succeeded -> {
                        Toast.makeText(activity, "Payment successful 🎉", Toast.LENGTH_LONG).show()
                        showSuccess()

                        activity.lifecycleScope.launch {
                            try {
                                val amt = getLastTopupAmount().takeIf { it > 0 } ?: 1000
                                val uid = prismaUserIdState
                                if (uid.isNullOrBlank()) {
                                    Toast.makeText(activity, "User not ready. Please login again.", Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                                GlobalContext.get().get<NobleRepository>().topUpWallet(uid, amt)
                                bumpRefresh()
                            } catch (e: Exception) {
                                Toast.makeText(activity, "Topup error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    StripeIntent.Status.RequiresPaymentMethod,
                    StripeIntent.Status.Canceled -> {
                        val msg = err ?: "Payment failed"
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                        showError(msg)
                    }

                    else -> {
                        val msg = "Payment status: $status"
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                        showError(msg)
                    }
                }
            }

            override fun onError(e: Exception) {
                setIsPaying(false)
                val msg = e.message ?: "Payment error"
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                showError(msg)
            }
        }
    )
}
