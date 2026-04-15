package com.eathioPay.demo

import android.app.Application
import com.eathioPay.demo.core.di.appModule
import com.kbyai.idsdk.IDSDK
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import io.scanbot.sdk.ScanbotSDKInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    val LICENSE_KEY =
        "nlHIQ2d3JzUiJO4U4BN2HUgw6xuXMH" +
            "tXh1kTSODETHIjag0ujajr/r5y0nzJ" +
            "EAtNKcQ0xYx7L5aLKJjgF5GmyddbTA" +
            "/uaAhrIeBXYdCGgA5He/WP7FElFw1M" +
            "sNz+6C4zzX+inxmEas+FY+dvTLzzfs" +
            "l1dsI4R2r9VBJIQkVRxesG0H8Wz5V/" +
            "fi3xEpACMZy+w/M4RyvQcTYLmaR2la" +
            "tLYVQGzbB+XWs+gBvZD5KxNfzsdMjH" +
            "nBF7yIXDBBqPFxaWllXsYrhwIjZcDY" +
            "nsHeauAZWWsEsvGqfUA+1AM0Pl3X5B" +
            "jBn2rmIaFJzQ7WPoJ64UOc7CUyZhy2" +
            "qusRtOkS6DnA==\nU2NhbmJvdFNESw" +
            "pjb20uZWF0aGlvUGF5LmRlbW8KMTc3" +
            "NjkwMjM5OQo4Mzg4NjA3CjE5\n"

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51RbPA2FYLQId6JpF6JCXr9IbaBBPOiCmz2h1Xi4eEzwflSlarggnks3OAPrExquHolTmNgvKI4NbSSigUYHRBBJw00wmWUf0xk"
        )
        Stripe(
            context = applicationContext,
            publishableKey = "pk_test_51RbPA2FYLQId6JpF6JCXr9IbaBBPOiCmz2h1Xi4eEzwflSlarggnks3OAPrExquHolTmNgvKI4NbSSigUYHRBBJw00wmWUf0xk"
        )

        ScanbotSDKInitializer()
            .license(this, LICENSE_KEY)
            .initialize(this)

        var ret = IDSDK.setActivation(
            "gjmeRzL+KESdnwtDwp6pHz5YSoKrT7KSrYob45qoWbkOxxpxH0XZKtAuIwWnjnr791L76sNnWGqXJBrbqdTiQ" +
                "Yy2NUptVy3+Y9jte1h4ifK6lx9oFjOvsVXWFc0rtMwQCSq1mL3zqVDsD7LaOVCHFwtsKSTfnOX04kz" +
                "sTJkBxGi9pI39cNQy2tyg1nWOrOQmr86Ng6Im+IW94kw4ZoxLnkrkJo1GV8ROeHOFKeHuyUbPm1YfZ" +
                "hquSkc2PydQc1UVCSjUGf8J1wdjom0aA/ctnl3P3d2MZBcQMmhK7KIVbOz4glbkmi64xy7jiDOJweV" +
                "5slLKF2fv0DR+x6/rYo4K7g=="
        )
        if (ret == IDSDK.SDK_SUCCESS) {
            ret = IDSDK.init(this)
            android.util.Log.d("IDSDK", "init ret = $ret")
        } else {
            android.util.Log.e("IDSDK", "Activation failed, ret = $ret")
        }
    }
}
