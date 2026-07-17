package com.example.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeepTabDto(
    @SerialName("id") val id: String,
    @SerialName("tracker_uid") val trackerUid: String,
    @SerialName("tracked_uid") val trackedUid: String,
    @SerialName("created_at") val createdAt: Instant
)
