package com.example.domain.model

import kotlinx.datetime.Instant

data class SecondingSnapshot(
    val predictionId: String,
    val timestamp: Instant,
    val totalSeconds: Int,
    val outcomes: List<SecondingSnapshotOutcome>
)

data class SecondingSnapshotOutcome(
    val outcomeId: String,
    val percentage: Int,
    val seconds: Int
)
