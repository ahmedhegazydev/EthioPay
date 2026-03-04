package com.kbyai.idcardrecognition.main

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kbyai.idcardrecognition.auth_gate.AuthGate
import com.kbyai.idcardrecognition.login.FirebasePhoneAuthScreen
import com.kbyai.idcardrecognition.payment.demo.HomeDemo
import com.kbyai.idcardrecognition.payment.demo.clearFaceVerified
import com.kbyai.idcardrecognition.payment.demo.setFaceVerified
import com.kbyai.idcardrecognition.payment.idcardrecognition.KycRoute
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.kbyai.idcardrecognition.payment.models.Screen
import com.google.firebase.auth.FirebaseAuth
import com.kbyai.idcardrecognition.payment.ver_id_2.FaceLoginButton
import com.stripe.android.Stripe
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    stripe: Stripe,
    scannedCard: ScannedCardUi?,
    onScanClick: () -> Unit,
    setLastTopupAmount: (Int) -> Unit,
    getRefreshSignal: () -> Int,
    bumpRefresh: () -> Unit,
    getIsPaying: () -> Boolean,
    setIsPaying: (Boolean) -> Unit,
    setScanDebugDialog: (String) -> Unit,
    setErrorDialog: (String?) -> Unit,
) {
    val navController = rememberNavController()
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scope = rememberCoroutineScope()

    var prismaUserId by rememberSaveable { mutableStateOf(prismaUserIdState) }
    LaunchedEffect(prismaUserId) { prismaUserIdState = prismaUserId }

    NavHost(navController = navController, startDestination = Screen.Gate.route) {

        composable(Screen.Gate.route) {
            AuthGate(
                navigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Gate.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                setPrismaUserId = { prismaUserId = it }
            )
            Text("Loading...")
        }

        composable(Screen.Login.route) {
            FirebasePhoneAuthScreen(
                onLoginSuccess = { id ->
                    prismaUserId = id
                    prismaUserIdState = id
                    navController.navigate(Screen.Face.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }


        composable(Screen.Face.route) {
            FaceLoginButton(
                onLoggedIn = {
                    scope.launch {
                        setFaceVerified(ctx, true)
                        navController.navigate(Screen.Kyc.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Kyc.route) {
            KycRoute(
                navController = navController,
                onResult = { json ->
                    lifecycleOwner.lifecycleScope.launchWhenStarted {
                        handleKycResult(
                            context = ctx,
                            navController = navController,
                            json = json,
                            showScanDebug = setScanDebugDialog,
                            showError = setErrorDialog
                        )
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            if (prismaUserId == null) {
                Text("Finishing login...")
            } else {
                HomeDemo(
                    userId = prismaUserId!!,
                    scannedCard = scannedCard,
                    onScanClick = onScanClick,
                    onLogOutClicked = {

                        FirebaseAuth.getInstance().signOut()
                        prismaUserId = null
                        scope.launch {
                            clearFaceVerified(ctx)
                        }
                    },
                    stripe = stripe,
                    isPaying = getIsPaying(),
                    setIsPaying = setIsPaying,
                    refreshSignal = getRefreshSignal(),
                    bumpRefresh = bumpRefresh
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppNavGraphPreview() {
    val ctx = LocalContext.current

    val stripe = Stripe(ctx, "pk_test_preview_key")

    AppNavGraph(
        stripe = stripe,
        scannedCard = null as ScannedCardUi?,
        onScanClick = {},
        setLastTopupAmount = {},
        getRefreshSignal = { 0 },
        bumpRefresh = {},
        getIsPaying = { false },
        setIsPaying = {},
        setScanDebugDialog = {},
        setErrorDialog = {},
    )
}
