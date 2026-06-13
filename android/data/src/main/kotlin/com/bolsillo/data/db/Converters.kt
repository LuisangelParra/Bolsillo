package com.bolsillo.data.db

import androidx.room.TypeConverter
import com.bolsillo.domain.model.AccountType
import com.bolsillo.domain.model.TransactionType

class Converters {
    @TypeConverter fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
