package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seconds")
data class SecondRecord(
    @PrimaryKey val id: String,
    val predictionId: String,
    val optionId: String,
    val reasoning: String,
    val castAtEpochMs: Long,
    val status: String,
    val crowdPercentAtSecond: Int? = null,
    val reasoningLockedAtEpochMs: Long? = null,
    val secondLockedAtEpochMs: Long? = null
)
