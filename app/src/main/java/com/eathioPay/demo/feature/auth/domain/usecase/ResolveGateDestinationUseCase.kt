package com.eathioPay.demo.feature.auth.domain.usecase

import com.eathioPay.demo.core.session.SessionSnapshot
import com.eathioPay.demo.payment.models.Screen

class ResolveGateDestinationUseCase {
    operator fun invoke(snapshot: SessionSnapshot): String = when {
        snapshot.userId.isNullOrBlank() -> Screen.Login.route
        !snapshot.isSessionUnlocked -> Screen.Face.route
        !snapshot.isKycDone -> Screen.Kyc.route
        else -> Screen.Home.route
    }
}
