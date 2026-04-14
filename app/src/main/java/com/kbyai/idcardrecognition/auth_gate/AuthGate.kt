package com.kbyai.idcardrecognition.auth_gate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.kbyai.idcardrecognition.login.AuthSessionGateway
import com.kbyai.idcardrecognition.login.SavedAuthSnapshot
import com.kbyai.idcardrecognition.payment.demo.clearFaceVerified
import com.kbyai.idcardrecognition.payment.demo.isKycDone
import com.kbyai.idcardrecognition.payment.demo.isSessionUnlocked
import com.kbyai.idcardrecognition.payment.models.Screen
import org.koin.compose.koinInject

internal enum class AuthGateDestination(val route: String) {
    Login(Screen.Login.route),
    Face(Screen.Face.route),
    Kyc(Screen.Kyc.route),
    Home(Screen.Home.route),
}

internal fun resolveAuthGateDestination(
    hasFirebaseUser: Boolean,
    snapshot: SavedAuthSnapshot,
    sessionUnlocked: Boolean,
    kycDone: Boolean,
): AuthGateDestination = when {
    !hasFirebaseUser && snapshot.prismaUserId.isNullOrBlank() -> AuthGateDestination.Login
    snapshot.prismaUserId.isNullOrBlank() -> AuthGateDestination.Login
    !sessionUnlocked -> AuthGateDestination.Face
    !kycDone -> AuthGateDestination.Kyc
    else -> AuthGateDestination.Home
}

@Composable
fun AuthGate(
    navigateTo: (String) -> Unit,
    setPrismaUserId: (String?) -> Unit,
) {
    val firebaseUser by rememberFirebaseUser()
    val context = LocalContext.current
    val sessionGateway: AuthSessionGateway = koinInject()

    LaunchedEffect(firebaseUser) {
        var snapshot = sessionGateway.getSavedAuthSnapshot()

        if (snapshot.prismaUserId.isNullOrBlank() && firebaseUser != null) {
            val restored = sessionGateway.exchangeFirebaseAuth(
                firebaseUid = firebaseUser!!.uid,
                phone = firebaseUser!!.phoneNumber,
            )

            if (restored.isSuccess) {
                val prismaUserId = restored.getOrNull().orEmpty()
                sessionGateway.persistResolvedAuth(
                    prismaUserId = prismaUserId,
                    firebaseUid = firebaseUser!!.uid,
                )
                snapshot = SavedAuthSnapshot(
                    prismaUserId = prismaUserId,
                    firebaseUid = firebaseUser!!.uid,
                )
            } else {
                sessionGateway.clearAuthData()
                snapshot = SavedAuthSnapshot()
            }
        }

        setPrismaUserId(snapshot.prismaUserId)

        val destination = resolveAuthGateDestination(
            hasFirebaseUser = firebaseUser != null,
            snapshot = snapshot,
            sessionUnlocked = isSessionUnlocked(context),
            kycDone = isKycDone(context),
        )

        if (destination == AuthGateDestination.Login) {
            clearFaceVerified(context)
        }

        navigateTo(destination.route)
    }
}
