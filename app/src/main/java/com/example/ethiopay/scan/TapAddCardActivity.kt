package com.example.ethiopay.scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import company.tap.gosellapi.GoSellSDK
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

  private var sdkSession: SDKSession? = null

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
        TODO("Not yet implemented")
    }

    override fun paymentFailed(charge: Charge?) {
        TODO("Not yet implemented")
    }

    override fun authorizationSucceed(authorize: Authorize) {
        TODO("Not yet implemented")
    }

    override fun authorizationFailed(authorize: Authorize?) {
        TODO("Not yet implemented")
    }

    override fun cardSaved(charge: Charge) {
        TODO("Not yet implemented")
    }

    override fun cardSavingFailed(charge: Charge) {
        TODO("Not yet implemented")
    }

    // ===== SessionDelegate callbacks =====
  override fun cardTokenizedSuccessfully(token: Token) {
    // رجّع token للـ caller
    val data = intent.apply {
      putExtra("tap_token_id", token.id) // token.getId()
    }
    setResult(RESULT_OK, data)
    finish()
  }

    override fun savedCardsList(cardsList: CardsList) {
        TODO("Not yet implemented")
    }

    override fun sdkError(goSellError: GoSellError?) {
    setResult(RESULT_CANCELED)
    finish()
  }

    override fun sessionIsStarting() {
        TODO("Not yet implemented")
    }

    override fun sessionHasStarted() {
        TODO("Not yet implemented")
    }

    override fun sessionCancelled() {
    setResult(RESULT_CANCELED)
    finish()
  }

    override fun sessionFailedToStart() {
        TODO("Not yet implemented")
    }

    override fun invalidCardDetails() {
        TODO("Not yet implemented")
    }

    override fun backendUnknownError(message: String?) {
        TODO("Not yet implemented")
    }

    override fun invalidTransactionMode() {
        TODO("Not yet implemented")
    }

    override fun invalidCustomerID() {
        TODO("Not yet implemented")
    }

    override fun userEnabledSaveCardOption(saveCardEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun cardTokenizedSuccessfully(
        token: Token,
        saveCardEnabled: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun asyncPaymentStarted(charge: Charge) {
        TODO("Not yet implemented")
    }

    override fun paymentInitiated(charge: Charge?) {
        TODO("Not yet implemented")
    }

    override fun getCurrency(): TapCurrency {
        TODO("Not yet implemented")
    }

    override fun getCustomer(): Customer {
        TODO("Not yet implemented")
    }

    override fun getAmount(): BigDecimal? {
        TODO("Not yet implemented")
    }

    override fun getItems(): ArrayList<PaymentItem?>? {
        TODO("Not yet implemented")
    }

    override fun getTransactionMode(): TransactionMode? {
        TODO("Not yet implemented")
    }

    override fun getTaxes(): ArrayList<Tax?>? {
        TODO("Not yet implemented")
    }

    override fun getShipping(): ArrayList<Shipping?>? {
        TODO("Not yet implemented")
    }

    override fun getPostURL(): String? {
        TODO("Not yet implemented")
    }

    override fun getPaymentDescription(): String? {
        TODO("Not yet implemented")
    }

    override fun getPaymentMetadata(): HashMap<String?, String?>? {
        TODO("Not yet implemented")
    }

    override fun getPaymentReference(): Reference? {
        TODO("Not yet implemented")
    }

    override fun getPaymentStatementDescriptor(): String? {
        TODO("Not yet implemented")
    }

    override fun getAllowedToSaveCard(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRequires3DSecure(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getReceiptSettings(): Receipt? {
        TODO("Not yet implemented")
    }

    override fun getAuthorizeAction(): AuthorizeAction? {
        TODO("Not yet implemented")
    }

    override fun getDestination(): Destinations? {
        TODO("Not yet implemented")
    }

    override fun getMerchant(): Merchant? {
        TODO("Not yet implemented")
    }

    override fun getPaymentDataType(): String? {
        TODO("Not yet implemented")
    }

    override fun getCardType(): CardType? {
        TODO("Not yet implemented")
    }

    override fun getDefaultCardHolderName(): String? {
        TODO("Not yet implemented")
    }

    override fun getEnableEditCardHolderName(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCardIssuer(): CardIssuer? {
        TODO("Not yet implemented")
    }

    override fun getTopUp(): TopUp? {
        TODO("Not yet implemented")
    }

    // باقي callbacks لازم تتعمل (حتى لو فاضية) حسب الـ interface
}
