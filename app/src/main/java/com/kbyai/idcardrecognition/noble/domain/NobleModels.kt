package com.kbyai.idcardrecognition.noble.domain

data class NobleWallet(
    val userId: String,
    val currency: String,
    val balance: Int,
    val updatedAt: Long = System.currentTimeMillis(),
)

data class NobleTransaction(
    val id: String,
    val ownerUserId: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Int,
    val currency: String,
    val status: String,
    val note: String?,
    val createdAt: String,
)

data class NobleHomeSnapshot(
    val wallet: NobleWallet?,
    val transactions: List<NobleTransaction>,
)
