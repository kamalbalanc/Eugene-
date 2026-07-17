package com.example.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PredictionDto(
    @SerialName("id") val id: String,
    @SerialName("category") val category: String,
    @SerialName("status") val status: String,
    @SerialName("publish_status") val publishStatus: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("rules_description") val rulesDescription: String,
    @SerialName("hero_image_url") val heroImageUrl: String? = null,
    @SerialName("outcome_images_source") val outcomeImagesSource: String? = null,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("closes_at") val closesAt: Instant,
    @SerialName("resolves_at") val resolvesAt: Instant,
    @SerialName("total_seconds") val totalSeconds: Int,
    @SerialName("options") val options: List<OptionDto>,
    @SerialName("resolution_source") val resolutionSource: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("approved_by") val approvedBy: String? = null,
    @SerialName("flag_count") val flagCount: Int = 0,
    @SerialName("resolved_outcome_id") val resolvedOutcomeId: String? = null,
    @SerialName("resolution_source_url") val resolutionSourceURL: String? = null,
    @SerialName("void_reason") val voidReason: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null
)

@Serializable
data class OptionDto(
    @SerialName("id") val id: String,
    @SerialName("text") val text: String,
    @SerialName("seconds") val seconds: Int,
    @SerialName("percentage") val percentage: Int,
    @SerialName("accent") val accent: String,
    @SerialName("image_url") val imageUrl: String? = null
)
