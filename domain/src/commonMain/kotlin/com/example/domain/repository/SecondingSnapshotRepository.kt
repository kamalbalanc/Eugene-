package com.example.domain.repository

import com.example.domain.model.SecondingSnapshot
import com.example.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface SecondingSnapshotRepository {
    fun observeSnapshots(predictionId: String, range: TimeRange): Flow<List<SecondingSnapshot>>
    fun observeDownsampledSnapshots(predictionId: String, range: TimeRange): Flow<List<SecondingSnapshot>>
    suspend fun recordSnapshot(predictionId: String, snapshot: SecondingSnapshot): Result<Unit>
    suspend fun computeDownsampledRollup(predictionId: String, granularity: String): Result<Unit>
    suspend fun sync(predictionId: String): Result<Unit>
}
