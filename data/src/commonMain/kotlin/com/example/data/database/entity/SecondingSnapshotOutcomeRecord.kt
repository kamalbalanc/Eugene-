package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "seconding_snapshot_outcomes",
    primaryKeys = ["predictionId", "timestamp", "outcomeId"],
    foreignKeys = [
        ForeignKey(
            entity = SecondingSnapshotRecord::class,
            parentColumns = ["predictionId", "timestamp"],
            childColumns = ["predictionId", "timestamp"]
        )
    ],
    indices = [Index(value = ["predictionId", "timestamp", "outcomeId"])]
)
data class SecondingSnapshotOutcomeRecord(
    val predictionId: String,
    val timestamp: Long,
    val outcomeId: String,
    val percentage: Int,
    val seconds: Int
)
