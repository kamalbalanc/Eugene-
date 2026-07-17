package com.example.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SecondingSnapshotDto(
    @SerialName("prediction_id") val predictionId: String,
    @SerialName("timestamp") val timestamp: Instant,
    @SerialName("total_seconds") val totalSeconds: Int,
    @SerialName("outcomes") val outcomes: List<SecondingSnapshotOutcomeDto>
)

@Serializable
data class SecondingSnapshotOutcomeDto(
    @SerialName("outcome_id") val outcomeId: String,
    @SerialName("percentage") val percentage: Int,
    @SerialName("seconds") val seconds: Int
)
