package com.kbyai.idcardrecognition.noble.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NobleApiService {

    @GET("wallet/{userId}")
    suspend fun getWallet(@Path("userId") userId: String): WalletResponse

    @GET("transactions/{userId}")
    suspend fun getTransactions(@Path("userId") userId: String): TransactionsResponse

    @POST("transfer")
    suspend fun transfer(@Body request: TransferRequest): TransactionEnvelopeResponse

    @POST("wallet/{userId}/topup")
    suspend fun topUp(
        @Path("userId") userId: String,
        @Body request: TopUpRequest,
    ): WalletResponse
}
