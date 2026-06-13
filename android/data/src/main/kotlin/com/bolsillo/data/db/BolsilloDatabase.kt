package com.bolsillo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bolsillo.data.db.dao.AccountDao
import com.bolsillo.data.db.dao.CategoryDao
import com.bolsillo.data.db.dao.TransactionDao
import com.bolsillo.data.db.entity.AccountEntity
import com.bolsillo.data.db.entity.CategoryEntity
import com.bolsillo.data.db.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class BolsilloDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        const val NAME = "bolsillo.db"
    }
}
