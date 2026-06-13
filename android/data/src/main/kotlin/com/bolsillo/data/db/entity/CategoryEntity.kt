package com.bolsillo.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name_key") val nameKey: String,
    val icon: String,
    @ColumnInfo(name = "color_token") val colorToken: String,
)
