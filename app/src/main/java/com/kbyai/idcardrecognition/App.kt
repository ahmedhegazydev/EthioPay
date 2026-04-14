package com.kbyai.idcardrecognition

import android.app.Application
import com.kbyai.idcardrecognition.di.appModule
import com.kbyai.idsdk.IDSDK
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
//import company.tap.gosellapi.GoSellSDK
import io.scanbot.sdk.ScanbotSDKInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    val LICENSE_KEY =
        "TXYcdCbmlLdtnVaGlOb7P9aK/2C+db" +
                "E8sbOcrh+WMxiS/DphLGZQnMk8MyJc" +
                "dampqb3bQrO2gGm2K4NVNc54CtZnV6" +
                "HNhng8l1iYqnAwMXH34x/VuPuL6fjp" +
                "VfwLhfFaRqbRg2V/8dNJmPXdnQk0Sk" +
                "XvBHHRmBGGrRQkt5VfcfHEKU6TavGr" +
                "uauFp8HOmOHsClSSWnW5dCm/g4PgN/" +
                "qd9Tu1YD/TwIRJ+aYgGP3SH106yHcK" +
                "gbVMTw18cc0IyvISObnJEe0JcAXcd3" +
                "mG0oDeWRSlbCFYmBZ1/X0Jj2mQiWax" +
                "y7tdZxLGRIxsOTTaI6ioJbrI9Ey924" +
                "hCCRwbYTUSQg==\nU2NhbmJvdFNESw" +
                "pjb20ua2J5YWkuaWRjYXJkcmVjb2du" +
                "aXRpb24KMTc3MDU5NTE5OQo4Mzg4Nj" +
                "A3CjE5\n";


    override fun onCreate() {
    super.onCreate()

      startKoin {
          androidContext(this@App)
          modules(appModule)
      }

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
           .license(this,LICENSE_KEY)
          .initialize(this)


      var ret = IDSDK.setActivation(
          "gjmeRzL+KESdnwtDwp6pHz5YSoKrT7KSrYob45qoWbkOxxpxH0XZKtAuIwWnjnr791L76sNnWGqXJBrbqdTiQ" +
                  "Yy2NUptVy3+Y9jte1h4ifK6lx9oFjOvsVXWFc0rtMwQCSq1mL3zqVDsD7LaOVCHFwtsKSTfnOX04kz" +
                  "sTJkBxGi9pI39cNQy2tyg1nWOrOQmr86Ng6Im+IW94kw4ZoxLnkrkJo1GV8ROeHOFKeHuyUbPm1YfZ" +
                  "hquSkc2PydQc1UVCSjUGf8J1wdjom0aA/ctnl3P3d2MZBcQMmhK7KIVbOz4glbkmi64xy7jiDOJweV" +
                  "5slLKF2fv0DR+x6/rYo4K7g=="
      )
      if(ret  == IDSDK.SDK_SUCCESS) {
          ret = IDSDK.init(this)
      }


  }
}
