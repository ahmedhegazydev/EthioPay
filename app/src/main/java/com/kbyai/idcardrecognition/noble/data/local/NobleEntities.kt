package com.kbyai.idcardrecognition.noble.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kbyai.idcardrecognition.noble.domain.NobleTransaction
import com.kbyai.idcardrecognition.noble.domain.NobleWallet

@Entity(tableName = "wallet_cache")
data class WalletCacheEntity(
    @PrimaryKey val userId: String,
    val currency: String,
    val balance: Int,
    val updatedAt: Long,
)

@Entity(tableName = "transaction_cache")
data class TransactionCacheEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Int,
    val currency: String,
    val status: String,
    val note: String?,
    val createdAt: String,
)

fun WalletCacheEntity.toDomain(): NobleWallet = NobleWallet(
    userId = userId,
    currency = currency,
    balance = balance,
    updatedAt = updatedAt,
)

fun NobleWallet.toEntity(): WalletCacheEntity = WalletCacheEntity(
    userId = userId,
    currency = currency,
    balance = balance,
    updatedAt = updatedAt,
)

fun TransactionCacheEntity.toDomain(): NobleTransaction = NobleTransaction(
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

fun NobleTransaction.toEntity(): TransactionCacheEntity = TransactionCacheEntity(
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
