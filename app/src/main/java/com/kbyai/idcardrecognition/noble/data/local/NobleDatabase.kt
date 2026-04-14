package com.kbyai.idcardrecognition.noble.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WalletCacheEntity::class, TransactionCacheEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NobleDatabase : RoomDatabase() {
    abstract fun nobleDao(): NobleDao
}
