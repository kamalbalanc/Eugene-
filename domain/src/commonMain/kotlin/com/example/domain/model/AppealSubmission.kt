package com.example.domain.model

import kotlinx.datetime.Instant

data class AppealSubmission(
    val id: String,
    val submittedBy: String,
    val submittedAt: Instant,
    val appealText: String,
    val state: AppealState
)

enum class AppealState { SUBMITTED, UNDER_REVIEW, UPHELD, REVERSED }
