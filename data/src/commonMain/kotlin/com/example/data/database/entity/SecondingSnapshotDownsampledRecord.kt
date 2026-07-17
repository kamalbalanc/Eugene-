package com.example.data.database.entity

import androidx.room.Entity

@Entity(
    tableName = "seconding_snapshot_downsampled",
    primaryKeys = ["predictionId", "bucketStart", "outcomeId", "granularity"]
)
data class SecondingSnapshotDownsampledRecord(
    val predictionId: String,
    val bucketStart: Long,
    val granularity: String,
    val outcomeId: String,
    val percentage: Int,
    val seconds: Int
)
