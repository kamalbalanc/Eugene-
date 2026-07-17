package com.example.data.repository

import com.example.data.database.dao.PredictionDao
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toRecord
import com.example.data.remote.EugeneApiService
import com.example.data.remote.mapper.toDomain
import com.example.domain.model.Prediction
import com.example.domain.model.PredictionPublishStatus
import com.example.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PredictionRepositoryImpl(
    private val predictionDao: PredictionDao,
    private val apiService: EugeneApiService
) : PredictionRepository {

    override fun observePredictions(): Flow<List<Prediction>> {
        return predictionDao.observePredictions().map { records ->
            records.map { it.toDomain() }
        }
    }

    override fun observePrediction(id: String): Flow<Prediction?> {
        return predictionDao.observePredictionById(id).map { it?.toDomain() }
    }

    override suspend fun upsert(prediction: Prediction): Result<Unit> {
        return try {
            predictionDao.insert(prediction.toRecord())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sync(): Result<Unit> {
        return try {
            val dtos = apiService.getPredictions()
            val records = dtos.map { it.toDomain().toRecord() }
            predictionDao.insertAll(records)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAll(): Result<Unit> {
        return try {
            predictionDao.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveLocalOnly(prediction: Prediction): Result<Unit> {
        return try {
            val localPrediction = prediction.copy(publishStatus = PredictionPublishStatus.LOCAL_ONLY)
            predictionDao.insert(localPrediction.toRecord())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reconcileApproved(predictionId: String, approvedPrediction: Prediction): Result<Unit> {
        return try {
            predictionDao.insert(approvedPrediction.toRecord())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
