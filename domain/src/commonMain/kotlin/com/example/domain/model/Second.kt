package com.example.domain.model

import kotlinx.datetime.Instant

data class Second(
    val id: String,
    val predictionId: String,
    val optionId: String,
    val reasoning: String,
    val castAt: Instant,
    val status: SecondStatus = SecondStatus.PENDING,
    val crowdPercentAtSecond: Int? = null,
    val reasoningLockedAt: Instant? = null,
    val secondLockedAt: Instant? = null
)

enum class SecondStatus { PENDING, CORRECT, INCORRECT, VOIDED }
