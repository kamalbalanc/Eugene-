package com.example.domain.repository

import com.example.domain.model.KeepTab
import kotlinx.coroutines.flow.Flow

interface KeepTabRepository {
    fun observeTracked(trackerUid: String): Flow<List<KeepTab>>
    fun isTracking(trackerUid: String, trackedUid: String): Flow<Boolean>
    suspend fun track(trackerUid: String, trackedUid: String): Result<Unit>
    suspend fun untrack(trackerUid: String, trackedUid: String): Result<Unit>
    suspend fun sync(): Result<Unit>
}
