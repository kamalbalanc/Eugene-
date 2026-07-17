package com.example.domain.model

import kotlinx.datetime.Instant

data class Comment(
    val id: String,
    val predictionId: String,
    val authorUid: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val secondedOptionId: String,
    val postedAt: Instant,
    val text: String,
    val helpfulCount: Int = 0,
    val lockedAt: Instant? = null,
    val flagCount: Int = 0
)
