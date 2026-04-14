package com.kbyai.idcardrecognition.noble.data

import com.kbyai.idcardrecognition.noble.data.local.NobleDao
import com.kbyai.idcardrecognition.noble.data.local.toDomain
import com.kbyai.idcardrecognition.noble.data.local.toEntity
import com.kbyai.idcardrecognition.noble.domain.NobleHomeSnapshot
import com.kbyai.idcardrecognition.noble.domain.NobleWallet

class NobleRepositoryImpl(
    private val cloudStore: NobleCloudStore,
    private val dao: NobleDao,
) : NobleRepository {

    override suspend fun loadCachedHome(userId: String): NobleHomeSnapshot {
        val wallet = dao.getWallet(userId)?.toDomain()
        val transactions = dao.getTransactions(userId).map { it.toDomain() }
        return NobleHomeSnapshot(wallet = wallet, transactions = transactions)
    }

    override suspend fun syncHome(userId: String): NobleHomeSnapshot {
        val remoteSnapshot = cloudStore.loadHome(userId)
        val remoteWallet = remoteSnapshot.wallet
        val remoteTransactions = remoteSnapshot.transactions

        if (remoteWallet != null) {
            dao.upsertWallet(remoteWallet.toEntity())
        }
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
        val snapshot = cloudStore.transferMoney(
            fromUserId = fromUserId,
            recipient = toUserId,
            amount = amount,
            note = note,
        )
        snapshot.wallet?.let { dao.upsertWallet(it.toEntity()) }
        dao.clearTransactions(fromUserId)
        dao.upsertTransactions(snapshot.transactions.map { it.toEntity() })
        return snapshot
    }

    override suspend fun topUpWallet(userId: String, amount: Int): NobleWallet {
        val wallet = cloudStore.topUpWallet(userId, amount)
        dao.upsertWallet(wallet.toEntity())
        return wallet
    }
}
