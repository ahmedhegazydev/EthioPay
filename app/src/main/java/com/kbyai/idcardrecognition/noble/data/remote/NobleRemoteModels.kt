package com.kbyai.idcardrecognition.noble.data.remote

import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.noble.domain.NobleWallet

data class WalletResponse(
    val userId: String,
    val currency: String,
    val balance: Int,
)

data class TransactionsResponse(
    val items: List<TransactionResponse>,
)

data class TransactionEnvelopeResponse(
    val tx: TransactionResponse,
)

data class TransactionResponse(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Int,
    val currency: String = "ETB",
    val status: String,
    val note: String?,
    val createdAt: String,
)

data class TransferRequest(
    val fromUserId: String,
    val toUserId: String,
    val amount: Int,
    val note: String?,
)

data class TopUpRequest(
    val amount: Int,
)

fun WalletResponse.toDomain(): NobleWallet = NobleWallet(
    userId = userId,
    currency = currency,
    balance = balance,
)

fun TransactionResponse.toDomain(ownerUserId: String): NobleTransaction = NobleTransaction(
    id = id,
    ownerUserId = ownerUserId,
    fromUserId = fromUserId,
    toUserId = toUserId,
    amount = amount,
    currency = currency,
    status = status,
    note = note,
    createdAt = createdAt,
)
