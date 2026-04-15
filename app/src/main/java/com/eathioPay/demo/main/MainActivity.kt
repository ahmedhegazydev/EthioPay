package com.eathioPay.demo.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.eathioPay.demo.payment.demo.setSessionUnlocked
import com.eathioPay.demo.security.AppSwitcherBlurOverlay
import com.eathioPay.demo.security.SecureScreen
import com.stripe.android.Stripe
import kotlinx.coroutines.launch

internal var prismaUserIdState: String? = null // بدل private var (عشان ملفات تانية تشوفه)

class MainActivity : FragmentActivity() {

    internal lateinit var stripe: Stripe
    private var blurForRecents by mutableStateOf(false)

    // state shared with Compose
    internal var lastTopupAmount: Int = 0
    internal var refreshSignal: Int = 0
    internal var isPaying: Boolean = false

    // dialogs state
    internal var showSuccessDialog: Boolean = false
    internal var showErrorDialog: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        // قبل super.onCreate
//        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        super.onCreate(savedInstanceState)

        stripe = Stripe(
            context = this,
            publishableKey = "pk_test_..." // زي ما هو
        )

        setContent {
            SecureScreen {
                // Card scan controller (launcher + scannedCard state)
                val scan = rememberCardScanController()

                MainApp(
                    stripe = stripe,
                    scannedCard = scan.scannedCard,
                    onScanClick = { scan.startScan() },
                    setLastTopupAmount = { lastTopupAmount = it },
                    getRefreshSignal = { refreshSignal },
                    bumpRefresh = { refreshSignal++ },
                    getIsPaying = { isPaying },
                    setIsPaying = { isPaying = it },
                    getSuccessDialog = { showSuccessDialog },
                    setSuccessDialog = { showSuccessDialog = it },
                    getErrorDialog = { showErrorDialog },
                    setErrorDialog = { showErrorDialog = it },
                )

                // overlay فوق كل حاجة
//                AppSwitcherBlurOverlay(show = blurForRecents)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        handleStripePaymentResult(
            activity = this,
            stripe = stripe,
            requestCode = requestCode,
            data = data,
            getLastTopupAmount = { lastTopupAmount },
            setIsPaying = { isPaying = it },
            showSuccess = { showSuccessDialog = true },
            showError = { msg -> showErrorDialog = msg },
            bumpRefresh = { refreshSignal++ }
        )
    }

    override fun onStart() {
        super.onStart()
        blurForRecents = false
    }

    override fun onStop() {
        // لما يروح للخلف/Recents
        blurForRecents = true
        super.onStop()
        lifecycleScope.launch { setSessionUnlocked(this@MainActivity, false) }
    }
}
