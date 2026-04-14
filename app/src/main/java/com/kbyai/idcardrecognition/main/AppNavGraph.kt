package com.kbyai.idcardrecognition.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kbyai.idcardrecognition.auth_gate.AuthGate
import com.kbyai.idcardrecognition.login.AuthSessionGateway
import com.kbyai.idcardrecognition.login.FirebasePhoneAuthScreen
import com.kbyai.idcardrecognition.payment.demo.HomeDemo
import com.kbyai.idcardrecognition.payment.demo.SplashScreen
import com.kbyai.idcardrecognition.payment.demo.clearFaceVerified
import com.kbyai.idcardrecognition.payment.demo.setFaceVerified
import com.kbyai.idcardrecognition.payment.demo.setSessionUnlocked
import com.kbyai.idcardrecognition.payment.idcardrecognition.KycRoute
import com.kbyai.idcardrecognition.payment.models.ScannedCardUi
import com.kbyai.idcardrecognition.payment.models.Screen
import com.kbyai.idcardrecognition.payment.ver_id_2.FaceLoginButton
import com.stripe.android.Stripe
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

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
    val authSessionGateway: AuthSessionGateway = koinInject()

    var prismaUserId by rememberSaveable { mutableStateOf(prismaUserIdState) }
    LaunchedEffect(prismaUserId) { prismaUserIdState = prismaUserId }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Gate.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Gate.route) {
            AuthGate(
                navigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Gate.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                setPrismaUserId = { prismaUserId = it },
            )
            LoadingScreen()
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
                        setSessionUnlocked(ctx, true)
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
                            showError = setErrorDialog,
                        )
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            if (prismaUserId == null) {
                LoadingScreen()
            } else {
                HomeDemo(
                    userId = prismaUserId!!,
                    scannedCard = scannedCard,
                    onScanClick = onScanClick,
                    onLogOutClicked = {
                        scope.launch {
                            authSessionGateway.clearAuthData()
                            prismaUserId = null
                            prismaUserIdState = null
                            clearFaceVerified(ctx)
                            navController.navigate(Screen.Gate.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    stripe = stripe,
                    isPaying = getIsPaying(),
                    setIsPaying = setIsPaying,
                    refreshSignal = getRefreshSignal(),
                    bumpRefresh = bumpRefresh,
                    setLastTopupAmount = setLastTopupAmount,
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .testTag("loading-brand")
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(28.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp),
                )
            }

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )

            Text(
                text = "Verifying your account...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
