package com.example.data.database.entity

import androidx.room.Entity

@Entity(
    tableName = "seconding_snapshots",
    primaryKeys = ["predictionId", "timestamp"]
)
data class SecondingSnapshotRecord(
    val predictionId: String,
    val timestamp: Long,
    val totalSeconds: Int
)
