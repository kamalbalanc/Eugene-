package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class PredictionRecord(
    @PrimaryKey val id: String,
    val category: String,
    val status: String?,
    val publishStatus: String,
    val title: String,
    val description: String,
    val rulesDescription: String,
    val heroImageUrl: String? = null,
    val outcomeImagesSource: String? = null,
    val createdAtEpochMs: Long,
    val closesAtEpochMs: Long,
    val resolvesAtEpochMs: Long,
    val totalSeconds: Int,
    val optionsJson: String,
    val resolutionSource: String,
    val createdBy: String,
    val approvedBy: String? = null,
    val flagCount: Int = 0,
    val resolvedOutcomeId: String? = null,
    val resolutionSourceURL: String? = null,
    val voidReason: String? = null,
    val rejectionReason: String? = null
)
