package com.example.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SecondDto(
    @SerialName("id") val id: String,
    @SerialName("prediction_id") val predictionId: String,
    @SerialName("option_id") val optionId: String,
    @SerialName("reasoning") val reasoning: String,
    @SerialName("cast_at") val castAt: Instant,
    @SerialName("status") val status: String,
    @SerialName("crowd_percent_at_second") val crowdPercentAtSecond: Int? = null,
    @SerialName("reasoning_locked_at") val reasoningLockedAt: Instant? = null,
    @SerialName("second_locked_at") val secondLockedAt: Instant? = null
)
