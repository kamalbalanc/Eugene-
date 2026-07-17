package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "keep_tabs",
    indices = [Index(value = ["trackerUid", "trackedUid"], unique = true)]
)
data class KeepTabRecord(
    @PrimaryKey val id: String,
    val trackerUid: String,
    val trackedUid: String,
    val createdAtEpochMs: Long,
    val syncedAt: Long? = null
)
