package com.kbyai.idcardrecognition.noble.data

import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleWallet

interface NobleRepository {
    suspend fun loadCachedHome(userId: String): NobleHomeSnapshot
    suspend fun syncHome(userId: String): NobleHomeSnapshot
    suspend fun transferMoney(
        fromUserId: String,
        toUserId: String,
        amount: Int,
        note: String?,
    ): NobleHomeSnapshot

    suspend fun topUpWallet(userId: String, amount: Int): NobleWallet
}
