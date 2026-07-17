package com.example.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscourseDto(
    @SerialName("id") val id: String,
    @SerialName("prediction_id") val predictionId: String,
    @SerialName("author_uid") val authorUid: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_handle") val authorHandle: String,
    @SerialName("author_avatar_url") val authorAvatarUrl: String,
    @SerialName("posted_at") val postedAt: Instant,
    @SerialName("text") val text: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("helpful_count") val helpfulCount: Int = 0,
    @SerialName("flag_count") val flagCount: Int = 0
)
