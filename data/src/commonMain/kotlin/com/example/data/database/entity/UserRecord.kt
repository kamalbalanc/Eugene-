package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserRecord(
    @PrimaryKey val uid: String,
    val email: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val accuracy: Int,
    val reputation: Int,
    val resolvedPredictionCount: Int = 0
)
