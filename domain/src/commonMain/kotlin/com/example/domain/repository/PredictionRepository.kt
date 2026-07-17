package com.example.domain.repository

import com.example.domain.model.Prediction
import kotlinx.coroutines.flow.Flow

interface PredictionRepository {
    fun observePredictions(): Flow<List<Prediction>>
    fun observePrediction(id: String): Flow<Prediction?>
    suspend fun upsert(prediction: Prediction): Result<Unit>
    suspend fun sync(): Result<Unit>
    suspend fun clearAll(): Result<Unit>
    suspend fun saveLocalOnly(prediction: Prediction): Result<Unit>
    suspend fun reconcileApproved(predictionId: String, approvedPrediction: Prediction): Result<Unit>
}
