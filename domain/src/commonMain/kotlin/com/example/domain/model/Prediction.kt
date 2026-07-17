package com.example.domain.model

import kotlinx.datetime.Instant

data class Prediction(
    val id: String,
    val category: PredictionCategory,
    val status: PredictionStatus,
    val publishStatus: PredictionPublishStatus,
    val title: String,
    val description: String,
    val rulesDescription: String,
    val heroImageUrl: String? = null,
    val outcomeImagesSource: ImageSource? = null,
    val createdAt: Instant,
    val closesAt: Instant,
    val resolvesAt: Instant,
    val totalSeconds: Int,
    val options: List<PredictionOption>,
    val resolutionSource: String,
    val createdBy: String,
    val approvedBy: String? = null,
    val flagCount: Int = 0,
    val resolvedOutcomeId: String? = null,
    val resolutionSourceURL: String? = null,
    val voidReason: String? = null,
    val rejectionReason: String? = null
) {
    init {
        require(options.size in 2..6) {
            "A Prediction must have between 2 and 6 outcomes"
        }
        require(resolvesAt > closesAt) {
            "resolvesAt must be strictly after closesAt"
        }
        val imageCount = options.count { it.imageUrl != null }
        require(imageCount == 0 || imageCount == options.size) {
            "PredictionOption images must be either entirely absent or present on every outcome"
        }
    }
}

data class PredictionOption(
    val id: String,
    val text: String,
    val seconds: Int,
    val percentage: Int,
    val accent: PredictionAccent,
    val imageUrl: String? = null
)

enum class PredictionAccent { SAGE, ORANGE, BLUE, AMBER, PURPLE, TEAL }

enum class ImageSource { PREDEFINED, USER_UPLOADED, PLACEHOLDER }

enum class PredictionCategory {
    POLITICS, SPORTS, ECONOMY, CULTURE, TECHNOLOGY, BUSINESS, ENTERTAINMENT, SCIENCE;

    companion object {
        fun fromString(raw: String): PredictionCategory =
            entries.find { it.name.equals(raw, ignoreCase = true) } ?: POLITICS
    }
}

enum class PredictionStatus { PENDING, APPROVED, LIVE, RESOLVING, RESOLVED, VOIDED, REJECTED }

enum class PredictionPublishStatus { LOCAL_ONLY, PENDING, APPROVED, REJECTED }
