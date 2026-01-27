package com.example.ethiopay

import android.app.Application
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import company.tap.gosellapi.GoSellSDK
import io.scanbot.sdk.ScanbotSDKInitializer

class App : Application() {
  override fun onCreate() {
    super.onCreate()

//    GoSellSDK.init(this, "sk_test_xxx", packageName)
//    GoSellSDK.setLocale("ar") // أو "ar"


      PaymentConfiguration.init(
          applicationContext,
          "pk_test_51RbPA2FYLQId6JpF6JCXr9IbaBBPOiCmz2h1Xi4eEzwflSlarggnks3OAPrExquHolTmNgvKI4NbSSigUYHRBBJw00wmWUf0xk"
      )
      Stripe(
          context = applicationContext,
          publishableKey = "pk_test_51RbPA2FYLQId6JpF6JCXr9IbaBBPOiCmz2h1Xi4eEzwflSlarggnks3OAPrExquHolTmNgvKI4NbSSigUYHRBBJw00wmWUf0xk"
      )

      ScanbotSDKInitializer()
          // Optional: Uncomment the next line if you have a license key.
          // .license(this, LICENSE_KEY)
          .initialize(this)


  }
}