package com.bolsillo.data.di

import com.bolsillo.data.repository.InMemoryTransactionRepository
import com.bolsillo.data.repository.SeedAccountRepository
import com.bolsillo.domain.port.AccountRepository
import com.bolsillo.domain.port.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds data-layer implementations to their domain ports. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindTransactionRepository(impl: InMemoryTransactionRepository): TransactionRepository

    @Binds
    abstract fun bindAccountRepository(impl: SeedAccountRepository): AccountRepository
}
