package com.example.domain.model

import kotlinx.datetime.Instant

data class KeepTab(
    val id: String,
    val trackerUid: String,
    val trackedUid: String,
    val createdAt: Instant
)
