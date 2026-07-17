package com.example.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("uid") val uid: String,
    @SerialName("email") val email: String,
    @SerialName("name") val name: String,
    @SerialName("handle") val handle: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("accuracy") val accuracy: Int,
    @SerialName("reputation") val reputation: Int,
    @SerialName("resolved_prediction_count") val resolvedPredictionCount: Int = 0
)
