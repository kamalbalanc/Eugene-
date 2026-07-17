package com.example.data.repository

import com.example.data.database.dao.DiscourseDao
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toRecord
import com.example.data.remote.EugeneApiService
import com.example.data.remote.mapper.toDto
import com.example.domain.model.DiscourseEntry
import com.example.domain.repository.DiscourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiscourseRepositoryImpl(
    private val discourseDao: DiscourseDao,
    private val apiService: EugeneApiService
) : DiscourseRepository {

    override fun observeDiscourse(predictionId: String): Flow<List<DiscourseEntry>> {
        return discourseDao.observeDiscourse(predictionId).map { records ->
            records.map { it.toDomain() }
        }
    }

    override suspend fun postEntry(entry: DiscourseEntry): Result<Unit> {
        return try {
            discourseDao.insert(entry.toRecord())
            apiService.postDiscourse(entry.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markHelpful(entryId: String): Result<Unit> {
        return try {
            discourseDao.incrementHelpfulCount(entryId)
            apiService.markDiscourseHelpful(entryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun flag(entryId: String): Result<Unit> {
        return try {
            discourseDao.incrementFlagCount(entryId)
            apiService.flagDiscourse(entryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
