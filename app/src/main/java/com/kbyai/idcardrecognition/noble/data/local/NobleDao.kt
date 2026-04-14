package com.kbyai.idcardrecognition.noble.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface NobleDao {

    @Query("SELECT * FROM wallet_cache WHERE userId = :userId LIMIT 1")
    suspend fun getWallet(userId: String): WalletCacheEntity?

    @Upsert
    suspend fun upsertWallet(wallet: WalletCacheEntity)

    @Query("SELECT * FROM transaction_cache WHERE ownerUserId = :userId ORDER BY createdAt DESC")
    suspend fun getTransactions(userId: String): List<TransactionCacheEntity>

    @Query("DELETE FROM transaction_cache WHERE ownerUserId = :userId")
    suspend fun clearTransactions(userId: String)

    @Upsert
    suspend fun upsertTransactions(items: List<TransactionCacheEntity>)
}
