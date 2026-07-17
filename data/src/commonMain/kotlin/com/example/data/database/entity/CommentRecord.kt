package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentRecord(
    @PrimaryKey val id: String,
    val predictionId: String,
    val authorUid: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val secondedOptionId: String,
    val postedAtEpochMs: Long,
    val text: String,
    val helpfulCount: Int = 0,
    val lockedAtEpochMs: Long? = null,
    val flagCount: Int = 0
)
