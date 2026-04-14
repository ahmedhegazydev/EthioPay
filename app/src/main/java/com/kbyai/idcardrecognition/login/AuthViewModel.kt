package com.kbyai.idcardrecognition.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kbyai.idcardrecognition.BuildConfig

class AuthViewModel(
    private val repo: PhoneAuthGateway,
    private val sessionGateway: AuthSessionGateway,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // ✅ timer job in VM
    private var resendJob: Job? = null
    private val RESEND_SECONDS = 120 // 2 minutes (غيّرها براحتك)

    fun onEvent(e: AuthEvent) {
        when (e) {
            is AuthEvent.PhoneChanged -> {
                _state.update {
                    it.copy(
                        phone = AuthValidation.sanitizePhoneInput(e.v),
                        message = null
                        // اختياري: لو عايز تمسح verificationId لما يغير الرقم:
                        // verificationId = null,
                        // otp = ""
                    )
                }
            }
            is AuthEvent.OtpChanged -> _state.update {
                it.copy(
                    otp = AuthValidation.normalizeOtp(e.v),
                    message = null,
                )
            }
            AuthEvent.BackToPhone -> {
                resendJob?.cancel()
                _state.update {
                    it.copy(
                        verificationId = null,
                        otp = "",
                        canResend = false,
                        resendSecondsLeft = 0,
                        isSendingOtp = false,
                        isVerifying = false,
                        isCompletingLogin = false,
                        message = null,
                    )
                }
            }

            is AuthEvent.SendOtp -> sendOtp(e.activity)
            AuthEvent.VerifyOtp -> verifyOtp()

            AuthEvent.ClearMessage -> _state.update { it.copy(message = null) }
        }
    }

    private fun sendOtp(activity: Activity?) {
        if (_state.value.isSendingOtp || _state.value.isCompletingLogin) return

        val phone = _state.value.phone.trim()
        val phoneError = AuthValidation.validatePhoneForOtp(phone)
        if (phoneError != null) {
            _state.update { it.copy(message = phoneError) }
            return
        }

        // ✅ prevent resend while timer running
        if (_state.value.resendSecondsLeft > 0) return

        // ✅ Simulation mode
        if (BuildConfig.SIMULATE_OTP) {
            startResendTimer()
            _state.update {
                it.copy(
                    isSendingOtp = false,
                    isVerifying = false,
                    verificationId = "SIMULATED",
                    canResend = true,
                    message = "تم إرسال الكود (Sim). استخدم 123456"
                )
            }
            return
        }

        val phoneE164 = AuthValidation.normalizePhone(phone)

        _state.update {
            it.copy(
                isSendingOtp = true,
                message = null
            )
        }

        viewModelScope.launch {
            repo.sendOtp(activity, phoneE164, resendToken).collect { r ->
                when (r) {
                    is PhoneAuthResult.CodeSent -> {
                        resendToken = r.token

                        // ✅ start timer AFTER CodeSent only
                        startResendTimer()

                        _state.update {
                            it.copy(
                                isSendingOtp = false,
                                verificationId = r.verificationId,
                                canResend = true,
                                message = "تم إرسال الكود"
                            )
                        }
                    }

                    is PhoneAuthResult.Verified -> {
                        completeVerifiedCredential(r.credential, source = "otp-auto")
                    }

                    is PhoneAuthResult.Error -> {
                        _state.update { it.copy(isSendingOtp = false, message = r.msg) }
                    }
                }
            }
        }
    }

    private fun verifyOtp() {
        val st = _state.value
        if (st.isVerifying || st.isCompletingLogin) return

        val validationError = AuthValidation.validateOtpSubmission(st)
        if (validationError != null) {
            _state.update { it.copy(message = validationError) }
            return
        }
        val vid = checkNotNull(st.verificationId)
        val normalizedOtp = AuthValidation.normalizeOtp(st.otp)

        // ✅ Simulation
        if (BuildConfig.SIMULATE_OTP) {
            if (normalizedOtp != "123456") {
                _state.update {
                    it.copy(
                        isVerifying = false,
                        isCompletingLogin = false,
                        prismaUserId = null,
                        message = "الكود غلط (استخدم 123456)",
                    )
                }
                return
            }
            completeSimulatedLogin()
            return
        }

        _state.update { it.copy(isVerifying = true, message = null) }

        val cred = repo.credentialFromCode(vid, normalizedOtp)
        completeVerifiedCredential(cred, source = "otp-code")
    }

    private fun startResendTimer() {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            for (t in RESEND_SECONDS downTo 1) {
                _state.update { it.copy(resendSecondsLeft = t) }
                delay(1000)
            }
            _state.update { it.copy(resendSecondsLeft = 0) }
        }
    }

    private fun completeSimulatedLogin() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isVerifying = false,
                    isCompletingLogin = true,
                    message = null,
                )
            }

            sessionGateway.completeSimulatedLogin()
                .onSuccess { prismaUserId ->
                    _state.update {
                        it.copy(
                            isCompletingLogin = false,
                            prismaUserId = prismaUserId,
                            message = "تم تسجيل الدخول (Sim)",
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isCompletingLogin = false,
                            prismaUserId = null,
                            message = throwable.localizedMessage ?: "Simulated login failed",
                        )
                    }
                }
        }
    }

    private fun completeVerifiedCredential(
        credential: com.google.firebase.auth.PhoneAuthCredential,
        source: String,
    ) {
        viewModelScope.launch {
            val signInResult = repo.signInWithCredential(credential)
            if (signInResult.isFailure) {
                _state.update {
                    it.copy(
                        isSendingOtp = false,
                        isVerifying = false,
                        isCompletingLogin = false,
                        prismaUserId = null,
                        message = signInResult.exceptionOrNull()?.localizedMessage ?: "فشل التحقق",
                    )
                }
                return@launch
            }

            val identity = repo.currentIdentity()
            if (identity == null) {
                _state.update {
                    it.copy(
                        isSendingOtp = false,
                        isVerifying = false,
                        isCompletingLogin = false,
                        prismaUserId = null,
                        message = "Signed in to Firebase, but no identity was returned.",
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    isSendingOtp = false,
                    isVerifying = false,
                    isCompletingLogin = true,
                    message = null,
                )
            }

            val exchangeResult = sessionGateway.exchangeFirebaseAuth(
                firebaseUid = identity.uid,
                phone = identity.phoneNumber ?: _state.value.phone,
            )

            if (exchangeResult.isSuccess) {
                val prismaUserId = exchangeResult.getOrNull().orEmpty().ifBlank { identity.uid }
                sessionGateway.persistResolvedAuth(
                    prismaUserId = prismaUserId,
                    firebaseUid = identity.uid,
                )
                _state.update {
                    it.copy(
                        isCompletingLogin = false,
                        prismaUserId = prismaUserId,
                        message = if (source == "otp-auto") {
                            "تم تسجيل الدخول تلقائيًا"
                        } else {
                            "تم تسجيل الدخول"
                        },
                    )
                }
            } else {
                sessionGateway.clearAuthData()
                val throwable = exchangeResult.exceptionOrNull()
                _state.update {
                    it.copy(
                        isCompletingLogin = false,
                        prismaUserId = null,
                        message = throwable?.localizedMessage ?: "Backend auth request failed",
                    )
                }
            }
        }
    }
}
