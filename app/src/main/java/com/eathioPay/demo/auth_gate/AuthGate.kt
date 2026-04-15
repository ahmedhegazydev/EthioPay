package com.eathioPay.demo.auth_gate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import com.eathioPay.demo.core.session.SessionStore
import com.eathioPay.demo.feature.auth.domain.usecase.ResolveGateDestinationUseCase

@Composable
fun AuthGate(
    navigateTo: (String) -> Unit,
    setPrismaUserId: (String?) -> Unit,
) {
    val sessionStore: SessionStore = koinInject()
    val resolveGateDestination: ResolveGateDestinationUseCase = koinInject()

    LaunchedEffect(Unit) {
        val snapshot = sessionStore.getSnapshot()
        setPrismaUserId(snapshot.userId)
        navigateTo(resolveGateDestination(snapshot))
    }
}
