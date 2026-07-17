package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appeal_submissions")
data class AppealSubmissionRecord(
    @PrimaryKey val id: String,
    val submittedBy: String,
    val submittedAtEpochMs: Long,
    val appealText: String,
    val state: String
)
