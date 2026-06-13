package com.bolsillo.data.repository

import com.bolsillo.data.db.dao.CategoryDao
import com.bolsillo.data.db.toDomain
import com.bolsillo.domain.model.Category
import com.bolsillo.domain.port.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCategoryRepository
    @Inject
    constructor(private val dao: CategoryDao) : CategoryRepository {
        override fun observeAll(): Flow<List<Category>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

        override suspend fun getById(id: String): Category? = dao.getById(id)?.toDomain()
    }
