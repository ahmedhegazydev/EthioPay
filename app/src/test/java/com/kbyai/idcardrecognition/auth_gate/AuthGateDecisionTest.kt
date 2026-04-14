package com.kbyai.idcardrecognition.auth_gate

import com.kbyai.idcardrecognition.login.SavedAuthSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthGateDecisionTest {

    @Test
    fun `no firebase user and no saved prisma id goes to login`() {
        val destination = resolveAuthGateDestination(
            hasFirebaseUser = false,
            snapshot = SavedAuthSnapshot(),
            sessionUnlocked = false,
            kycDone = false,
        )

        assertEquals(AuthGateDestination.Login, destination)
    }

    @Test
    fun `known account with locked session goes to biometric gate`() {
        val destination = resolveAuthGateDestination(
            hasFirebaseUser = true,
            snapshot = SavedAuthSnapshot(prismaUserId = "user-1", firebaseUid = "firebase-1"),
            sessionUnlocked = false,
            kycDone = false,
        )

        assertEquals(AuthGateDestination.Face, destination)
    }

    @Test
    fun `unlocked session with incomplete kyc goes to kyc`() {
        val destination = resolveAuthGateDestination(
            hasFirebaseUser = true,
            snapshot = SavedAuthSnapshot(prismaUserId = "user-1", firebaseUid = "firebase-1"),
            sessionUnlocked = true,
            kycDone = false,
        )

        assertEquals(AuthGateDestination.Kyc, destination)
    }

    @Test
    fun `unlocked session with completed kyc goes home`() {
        val destination = resolveAuthGateDestination(
            hasFirebaseUser = true,
            snapshot = SavedAuthSnapshot(prismaUserId = "user-1", firebaseUid = "firebase-1"),
            sessionUnlocked = true,
            kycDone = true,
        )

        assertEquals(AuthGateDestination.Home, destination)
    }
}
