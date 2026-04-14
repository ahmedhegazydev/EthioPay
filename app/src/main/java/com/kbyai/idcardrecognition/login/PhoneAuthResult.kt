package com.kbyai.idcardrecognition.login

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

sealed interface PhoneAuthResult {
  data class CodeSent(
    val verificationId: String,
    val token: PhoneAuthProvider.ForceResendingToken?,
  ) : PhoneAuthResult
  data class Verified(val credential: PhoneAuthCredential) : PhoneAuthResult
  data class Error(val msg: String) : PhoneAuthResult
}

interface PhoneAuthGateway {
  fun sendOtp(
    activity: Activity?,
    phoneE164: String,
    resendToken: PhoneAuthProvider.ForceResendingToken? = null
  ): kotlinx.coroutines.flow.Flow<PhoneAuthResult>

  suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<Unit>

  fun credentialFromCode(verificationId: String, code: String): PhoneAuthCredential

  fun currentIdentity(): FirebaseIdentity?
}

class PhoneAuthRepository(
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : PhoneAuthGateway {
  override
  fun sendOtp(
    activity: Activity?,
    phoneE164: String,
    resendToken: PhoneAuthProvider.ForceResendingToken?
  ) = callbackFlow<PhoneAuthResult> {
    if (activity == null) {
      trySend(PhoneAuthResult.Error("Activity is required to start phone verification"))
      close()
      return@callbackFlow
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
      override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        trySend(PhoneAuthResult.Verified(credential))
      }

      override fun onVerificationFailed(e: FirebaseException) {
        trySend(PhoneAuthResult.Error(e.localizedMessage ?: "Verification failed"))
      }

      override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
      ) {
        trySend(PhoneAuthResult.CodeSent(verificationId, token))
      }
    }

    val builder = PhoneAuthOptions.newBuilder(auth)
      .setPhoneNumber(phoneE164)
      .setTimeout(60L, TimeUnit.SECONDS)
      .setActivity(activity)
      .setCallbacks(callbacks)

    if (resendToken != null) builder.setForceResendingToken(resendToken)

    PhoneAuthProvider.verifyPhoneNumber(builder.build())

    awaitClose { /* no-op */ }
  }

  override
  suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<Unit> {
    return try {
      auth.signInWithCredential(credential).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override
  fun credentialFromCode(verificationId: String, code: String): PhoneAuthCredential =
    PhoneAuthProvider.getCredential(verificationId, code)

  override
  fun currentIdentity(): FirebaseIdentity? {
    val user = auth.currentUser ?: return null
    return FirebaseIdentity(
      uid = user.uid,
      phoneNumber = user.phoneNumber,
    )
  }
}
