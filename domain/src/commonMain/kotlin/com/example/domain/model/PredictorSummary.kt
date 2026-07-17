package com.example.domain.model

data class PredictorSummary(
    val uid: String,
    val avatarUrl: String,
    val accuracyRate: Int,
    val resolvedPredictionCount: Int
)
