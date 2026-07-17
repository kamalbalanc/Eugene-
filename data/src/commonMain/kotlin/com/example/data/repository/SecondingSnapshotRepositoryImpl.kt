package com.example.data.repository

import com.example.data.database.dao.SecondingSnapshotDao
import com.example.data.local.mapper.mapSnapshot
import com.example.data.local.mapper.toOutcomeRecords
import com.example.data.local.mapper.toRecord
import com.example.data.remote.EugeneApiService
import com.example.data.remote.mapper.toDto
import com.example.data.remote.mapper.toDomain
import com.example.domain.model.SecondingSnapshot
import com.example.domain.model.SecondingSnapshotOutcome
import com.example.domain.model.TimeRange
import com.example.domain.repository.SecondingSnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class SecondingSnapshotRepositoryImpl(
    private val snapshotDao: SecondingSnapshotDao,
    private val apiService: EugeneApiService
) : SecondingSnapshotRepository {

    override fun observeSnapshots(predictionId: String, range: TimeRange): Flow<List<SecondingSnapshot>> {
        return combine(
            snapshotDao.observeSnapshots(predictionId),
            snapshotDao.observeSnapshotOutcomes(predictionId)
        ) { records, outcomes ->
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val filterMs = when (range) {
                TimeRange.DAY -> nowMs - (24 * 60 * 60 * 1000L)
                TimeRange.WEEK -> nowMs - (7 * 24 * 60 * 60 * 1000L)
                TimeRange.ALL -> 0L
            }
            val filteredRecords = records.filter { it.timestamp >= filterMs }
            val outcomesMap = outcomes.groupBy { it.timestamp }
            
            filteredRecords.map { rec ->
                mapSnapshot(rec, outcomesMap[rec.timestamp] ?: emptyList())
            }
        }
    }

    override fun observeDownsampledSnapshots(predictionId: String, range: TimeRange): Flow<List<SecondingSnapshot>> {
        return snapshotDao.observeDownsampled(predictionId).map { records ->
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val filterMs = when (range) {
                TimeRange.DAY -> nowMs - (24 * 60 * 60 * 1000L)
                TimeRange.WEEK -> nowMs - (7 * 24 * 60 * 60 * 1000L)
                TimeRange.ALL -> 0L
            }
            val filteredRecords = records.filter { it.bucketStart >= filterMs }
            val grouped = filteredRecords.groupBy { it.bucketStart }
            
            grouped.map { (bucketStart, outcomeRecs) ->
                val totalSeconds = outcomeRecs.sumOf { it.seconds }
                SecondingSnapshot(
                    predictionId = predictionId,
                    timestamp = Instant.fromEpochMilliseconds(bucketStart),
                    totalSeconds = totalSeconds,
                    outcomes = outcomeRecs.map {
                        SecondingSnapshotOutcome(
                            outcomeId = it.outcomeId,
                            percentage = it.percentage,
                            seconds = it.seconds
                        )
                    }
                )
            }.sortedBy { it.timestamp }
        }
    }

    override suspend fun recordSnapshot(predictionId: String, snapshot: SecondingSnapshot): Result<Unit> {
        return try {
            snapshotDao.insertSnapshot(snapshot.toRecord())
            snapshotDao.insertSnapshotOutcomes(snapshot.toOutcomeRecords())
            apiService.recordSnapshot(predictionId, snapshot.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun computeDownsampledRollup(predictionId: String, granularity: String): Result<Unit> {
        return try {
            apiService.computeDownsampledRollup(predictionId, granularity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sync(predictionId: String): Result<Unit> {
        return try {
            val dtos = apiService.getSnapshots(predictionId, "ALL")
            for (dto in dtos) {
                val domain = dto.toDomain()
                snapshotDao.insertSnapshot(domain.toRecord())
                snapshotDao.insertSnapshotOutcomes(domain.toOutcomeRecords())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
