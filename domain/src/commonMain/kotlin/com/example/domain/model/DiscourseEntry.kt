package com.example.domain.model

import kotlinx.datetime.Instant

data class DiscourseEntry(
    val id: String,
    val predictionId: String,
    val authorUid: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val postedAt: Instant,
    val text: String,
    val parentId: String? = null,
    val helpfulCount: Int = 0,
    val flagCount: Int = 0
)
