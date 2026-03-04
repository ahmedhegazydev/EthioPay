package com.kbyai.idcardrecognition.auth_gate

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.kbyai.idcardrecognition.payment.demo.clearFaceVerified
import com.kbyai.idcardrecognition.payment.demo.getPrismaUserId
import com.kbyai.idcardrecognition.payment.demo.isKycDone
import com.kbyai.idcardrecognition.payment.demo.isSessionUnlocked
import com.kbyai.idcardrecognition.payment.models.Screen

@Composable
fun AuthGate(
    navigateTo: (String) -> Unit,
    setPrismaUserId: (String?) -> Unit
) {
    val firebaseUser by rememberFirebaseUser()
    val ctx = LocalContext.current

    LaunchedEffect(firebaseUser) {
        val pid = getPrismaUserId(ctx)
        setPrismaUserId(pid)

        if (firebaseUser == null && pid == null) {
            clearFaceVerified(ctx)
            navigateTo(Screen.Login.route)
            return@LaunchedEffect
        }

        val sessionOk = isSessionUnlocked(ctx)
        val kycDone = isKycDone(ctx)

        if (!sessionOk) {
            navigateTo(Screen.Face.route)
            return@LaunchedEffect
        }

        if (!kycDone) {
            navigateTo(Screen.Kyc.route)
            return@LaunchedEffect
        }

        navigateTo(Screen.Home.route)
    }
}
