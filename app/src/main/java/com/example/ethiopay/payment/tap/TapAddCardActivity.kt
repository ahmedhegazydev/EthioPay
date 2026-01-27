package com.example.ethiopay.payment.tap

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import company.tap.gosellapi.internal.api.callbacks.GoSellError
import company.tap.gosellapi.internal.api.models.Authorize
import company.tap.gosellapi.internal.api.models.CardIssuer
import company.tap.gosellapi.internal.api.models.Charge
import company.tap.gosellapi.internal.api.models.Merchant
import company.tap.gosellapi.internal.api.models.PhoneNumber
import company.tap.gosellapi.internal.api.models.Token
import company.tap.gosellapi.open.controllers.SDKSession
import company.tap.gosellapi.open.delegate.SessionDelegate
import company.tap.gosellapi.open.enums.CardType
import company.tap.gosellapi.open.enums.TransactionMode
import company.tap.gosellapi.open.interfaces.PaymentDataSource
import company.tap.gosellapi.open.models.AuthorizeAction
import company.tap.gosellapi.open.models.CardsList
import company.tap.gosellapi.open.models.Customer
import company.tap.gosellapi.open.models.Destinations
import company.tap.gosellapi.open.models.PaymentItem
import company.tap.gosellapi.open.models.Receipt
import company.tap.gosellapi.open.models.Reference
import company.tap.gosellapi.open.models.Shipping
import company.tap.gosellapi.open.models.TapCurrency
import company.tap.gosellapi.open.models.Tax
import company.tap.gosellapi.open.models.TopUp
import java.math.BigDecimal
import java.util.ArrayList
import java.util.HashMap

class TapAddCardActivity : ComponentActivity(), SessionDelegate, PaymentDataSource {

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

  private var sdkSession: SDKSession? = null

    private val tapCurrency = TapCurrency("ETB")
    private val tapCustomer = Customer.CustomerBuilder(null)
        .email("test@test.com")
        .firstName("Test")
        .lastName("User")
        .phone(PhoneNumber("251", "9xxxxxxxx"))
        .build()
    private val tapAmount = BigDecimal("0")
    private val tapMode = TransactionMode.TOKENIZE_CARD


    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // 1) جهز session
    sdkSession = SDKSession().apply {
      addSessionDelegate(this@TapAddCardActivity)
      instantiatePaymentDataSource()
      setTransactionCurrency(TapCurrency("ETB")) // أو العملة اللي حساب Tap عندك شغال بيها
      setCustomer(
        Customer.CustomerBuilder(null)
          .email("test@test.com")
          .firstName("Test")
          .lastName("User")
          .phone(PhoneNumber("251", "9xxxxxxxx"))
          .build()
      )
      setAmount(BigDecimal("0")) // tokenize/save غالبًا بيقبل 0
        transactionMode = TransactionMode.TOKENIZE_CARD // ✅ الهدف
      isUserAllowedToSaveCard(true) // لو عايز user يختار save card
    }

    // 2) ابدأ UI بتاع Tap
    sdkSession?.start(this)
  }
    override fun paymentSucceed(charge: Charge) {
        showToast("Payment succeeded ✅")
    }

    override fun paymentFailed(charge: Charge?) {
        showToast("Payment failed ❌")
    }

    override fun authorizationSucceed(authorize: Authorize) {
        showToast("Authorization succeeded ✅")
    }

    override fun authorizationFailed(authorize: Authorize?) {
        showToast("Authorization failed ❌")
    }

    override fun cardSaved(charge: Charge) {
        showToast("Card saved successfully 💳")
    }

    override fun cardSavingFailed(charge: Charge) {
        showToast("Card saving failed ❌")
    }

    override fun cardTokenizedSuccessfully(token: Token) {
        showToast("Card tokenized successfully ✅")

        val data = intent.apply {
            putExtra("tap_token_id", token.id)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun savedCardsList(cardsList: CardsList) {
        TODO("Not yet implemented")
    }

    override fun sdkError(goSellError: GoSellError?) {
//        showToast(goSellError?.message ?: "SDK error occurred ❌")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun sessionIsStarting() {
        showToast("Payment session is starting…")
    }

    override fun sessionHasStarted() {
        showToast("Payment session started")
    }

    override fun sessionCancelled() {
        showToast("Payment cancelled by user")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun sessionFailedToStart() {
        showToast("Failed to start payment session ❌")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun invalidCardDetails() {
        showToast("Invalid card details ❌")
    }

    override fun backendUnknownError(message: String?) {
        showToast(message ?: "Unknown backend error ❌")
    }

    override fun invalidTransactionMode() {
        showToast("Invalid transaction mode ❌")
    }

    override fun invalidCustomerID() {
        showToast("Invalid customer ID ❌")
    }

    override fun userEnabledSaveCardOption(saveCardEnabled: Boolean) {
        showToast(
            if (saveCardEnabled) "Save card enabled ✅"
            else "Save card disabled"
        )
    }

    override fun cardTokenizedSuccessfully(token: Token, saveCardEnabled: Boolean) {
        showToast(
            if (saveCardEnabled) "Card tokenized & saved 💾"
            else "Card tokenized ✅"
        )

        val data = intent.apply {
            putExtra("tap_token_id", token.id)
            putExtra("tap_save_card_enabled", saveCardEnabled)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun asyncPaymentStarted(charge: Charge) {
        showToast("Async payment started…")
    }

    override fun paymentInitiated(charge: Charge?) {
        showToast("Payment initiated…")
    }

    override fun getCurrency(): TapCurrency = tapCurrency

    override fun getCustomer(): Customer = tapCustomer

    override fun getAmount(): BigDecimal = tapAmount

    override fun getTransactionMode(): TransactionMode = tapMode

    override fun getItems(): ArrayList<PaymentItem?>? = null
    override fun getTaxes(): ArrayList<Tax?>? = null
    override fun getShipping(): ArrayList<Shipping?>? = null

    override fun getPostURL(): String? = null
    override fun getPaymentDescription(): String? = "Tokenize card"
    override fun getPaymentMetadata(): HashMap<String?, String?>? = hashMapOf()
    override fun getPaymentReference(): Reference? = null
    override fun getPaymentStatementDescriptor(): String? = null

    override fun getAllowedToSaveCard(): Boolean = true
    override fun getRequires3DSecure(): Boolean = false

    override fun getReceiptSettings(): Receipt? = null
    override fun getAuthorizeAction(): AuthorizeAction? = null
    override fun getDestination(): Destinations? = null
    override fun getMerchant(): Merchant? = null

    override fun getPaymentDataType(): String? = null
    override fun getCardType(): CardType? = null
    override fun getDefaultCardHolderName(): String? = null
    override fun getEnableEditCardHolderName(): Boolean = false
    override fun getCardIssuer(): CardIssuer? = null
    override fun getTopUp(): TopUp? = null

}