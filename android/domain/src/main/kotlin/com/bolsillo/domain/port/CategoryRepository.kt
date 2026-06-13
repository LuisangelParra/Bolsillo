package com.bolsillo.domain.port

import com.bolsillo.domain.model.Category
import kotlinx.coroutines.flow.Flow

/** Read-only category catalog (seeded from shared-assets taxonomy in 001). */
interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>

    suspend fun getById(id: String): Category?
}
