package com.example.domain.usecase.second

import com.example.domain.model.SecondingSnapshot
import com.example.domain.repository.SecondingSnapshotRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class RecordSecondingSnapshotUseCase(
    private val snapshotRepository: SecondingSnapshotRepository
) {
    suspend operator fun invoke(predictionId: String, currentDistribution: SecondingSnapshot) {
        val bucketStart = currentBucketStart(Clock.System.now())
        snapshotRepository.recordSnapshot(
            predictionId,
            currentDistribution.copy(timestamp = bucketStart)
        )
    }

    private fun currentBucketStart(now: Instant): Instant {
        val bucketMillis = 5 * 60 * 1000L
        val epochMs = now.toEpochMilliseconds()
        return Instant.fromEpochMilliseconds(epochMs - (epochMs % bucketMillis))
    }
}
