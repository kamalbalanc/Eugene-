package com.example.domain.repository

import com.example.domain.model.DiscourseEntry
import kotlinx.coroutines.flow.Flow

interface DiscourseRepository {
    fun observeDiscourse(predictionId: String): Flow<List<DiscourseEntry>>
    suspend fun postEntry(entry: DiscourseEntry): Result<Unit>
    suspend fun markHelpful(entryId: String): Result<Unit>
    suspend fun flag(entryId: String): Result<Unit>
}
