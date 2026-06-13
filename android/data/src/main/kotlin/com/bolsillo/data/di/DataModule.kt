package com.bolsillo.data.di

import android.content.Context
import androidx.room.Room
import com.bolsillo.data.ai.LastUsedExpenseClassifier
import com.bolsillo.data.db.BolsilloDatabase
import com.bolsillo.data.db.BolsilloMigrations
import com.bolsillo.data.db.SqlCipherKeyProvider
import com.bolsillo.data.db.dao.AccountDao
import com.bolsillo.data.db.dao.CategoryDao
import com.bolsillo.data.db.dao.TransactionDao
import com.bolsillo.data.repository.RoomAccountRepository
import com.bolsillo.data.repository.RoomCategoryRepository
import com.bolsillo.data.repository.RoomTransactionRepository
import com.bolsillo.domain.ai.ExpenseClassifier
import com.bolsillo.domain.port.AccountRepository
import com.bolsillo.domain.port.CategoryRepository
import com.bolsillo.domain.port.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: RoomTransactionRepository): TransactionRepository

    @Binds @Singleton
    abstract fun bindAccountRepository(impl: RoomAccountRepository): AccountRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: RoomCategoryRepository): CategoryRepository

    @Binds @Singleton
    abstract fun bindExpenseClassifier(impl: LastUsedExpenseClassifier): ExpenseClassifier
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideKeyProvider(
        @ApplicationContext context: Context,
    ): SqlCipherKeyProvider = SqlCipherKeyProvider(context)

    @Provides @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyProvider: SqlCipherKeyProvider,
    ): BolsilloDatabase {
        // Load the native SQLCipher libs once before opening the DB (4.6.x API).
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(keyProvider.passphrase())
        return Room.databaseBuilder(context, BolsilloDatabase::class.java, BolsilloDatabase.NAME)
            .openHelperFactory(factory)
            .addMigrations(*BolsilloMigrations.ALL)
            .build()
    }

    @Provides fun provideAccountDao(db: BolsilloDatabase): AccountDao = db.accountDao()

    @Provides fun provideTransactionDao(db: BolsilloDatabase): TransactionDao = db.transactionDao()

    @Provides fun provideCategoryDao(db: BolsilloDatabase): CategoryDao = db.categoryDao()
}
