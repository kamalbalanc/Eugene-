package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discourse_entries")
data class DiscourseRecord(
    @PrimaryKey val id: String,
    val predictionId: String,
    val authorUid: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val postedAtEpochMs: Long,
    val text: String,
    val parentId: String? = null,
    val helpfulCount: Int = 0,
    val flagCount: Int = 0
)
