package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "submissions")
data class SubmissionRecord(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val submittedBy: String,
    val submittedAtEpochMs: Long,
    val closesAtEpochMs: Long,
    val resolvesAtEpochMs: Long,
    val source: String,
    val criteria: String,
    val state: String,
    val outcomesJson: String
)
