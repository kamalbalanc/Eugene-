package com.example.data.repository

import com.example.data.database.dao.SecondDao
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toRecord
import com.example.data.remote.EugeneApiService
import com.example.data.remote.mapper.toDto
import com.example.domain.model.Second
import com.example.domain.repository.SecondRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SecondRepositoryImpl(
    private val secondDao: SecondDao,
    private val apiService: EugeneApiService
) : SecondRepository {

    override fun observeSeconds(): Flow<List<Second>> {
        return secondDao.observeSeconds().map { records ->
            records.map { it.toDomain() }
        }
    }

    override suspend fun castSecond(second: Second): Result<Unit> {
        return try {
            secondDao.insert(second.toRecord())
            apiService.castSecond(second.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
