package com.kbyai.idcardrecognition.login

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/firebase")
    suspend fun exchangeFirebaseAuth(
        @Body request: FirebaseAuthExchangeRequest,
    ): FirebaseAuthExchangeResponse
}
