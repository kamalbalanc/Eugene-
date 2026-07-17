package com.example.domain.model

import kotlinx.datetime.Instant

data class Submission(
    val id: String,
    val title: String,
    val category: PredictionCategory,
    val submittedBy: String,
    val submittedAt: Instant,
    val closesAt: Instant,
    val resolvesAt: Instant,
    val source: String,
    val criteria: String,
    val state: ApprovalState,
    val outcomes: List<String> = emptyList()
)

enum class ApprovalState { SUBMITTED, APPROVED, REJECTED }
