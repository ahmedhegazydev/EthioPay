package com.kbyai.idcardrecognition.noble.data

import com.kbyai.idcardrecognition.noble.data.local.NobleDao
import com.kbyai.idcardrecognition.noble.data.local.toDomain
import com.kbyai.idcardrecognition.noble.data.local.toEntity
import com.kbyai.idcardrecognition.noble.data.remote.NobleApiService
import com.kbyai.idcardrecognition.noble.data.remote.TopUpRequest
import com.kbyai.idcardrecognition.noble.data.remote.TransferRequest
import com.kbyai.idcardrecognition.noble.data.remote.toDomain
import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleWallet

class NobleRepositoryImpl(
    private val api: NobleApiService,
    private val dao: NobleDao,
) : NobleRepository {

    override suspend fun loadCachedHome(userId: String): NobleHomeSnapshot {
        val wallet = dao.getWallet(userId)?.toDomain()
        val transactions = dao.getTransactions(userId).map { it.toDomain() }
        return NobleHomeSnapshot(wallet = wallet, transactions = transactions)
    }

    override suspend fun syncHome(userId: String): NobleHomeSnapshot {
        val remoteWallet = api.getWallet(userId).toDomain()
        val remoteTransactions = api.getTransactions(userId).items.map { it.toDomain(userId) }

        dao.upsertWallet(remoteWallet.toEntity())
        dao.clearTransactions(userId)
        dao.upsertTransactions(remoteTransactions.map { it.toEntity() })

        return NobleHomeSnapshot(
            wallet = remoteWallet,
            transactions = remoteTransactions,
        )
    }

    override suspend fun transferMoney(
        fromUserId: String,
        toUserId: String,
        amount: Int,
        note: String?,
    ): NobleHomeSnapshot {
        api.transfer(
            TransferRequest(
                fromUserId = fromUserId,
                toUserId = toUserId,
                amount = amount,
                note = note,
            )
        )
        return syncHome(fromUserId)
    }

    override suspend fun topUpWallet(userId: String, amount: Int): NobleWallet {
        val wallet = api.topUp(userId, TopUpRequest(amount)).toDomain()
        dao.upsertWallet(wallet.toEntity())
        return wallet
    }
}
