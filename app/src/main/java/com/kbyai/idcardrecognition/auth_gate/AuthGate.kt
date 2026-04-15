package com.kbyai.idcardrecognition.auth_gate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        val pid = getPrismaUserId(ctx)
        setPrismaUserId(pid)

        if (pid.isNullOrBlank()) {
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
