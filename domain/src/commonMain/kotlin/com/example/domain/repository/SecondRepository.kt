package com.example.domain.repository

import com.example.domain.model.Second
import kotlinx.coroutines.flow.Flow

interface SecondRepository {
    fun observeSeconds(): Flow<List<Second>>
    suspend fun castSecond(second: Second): Result<Unit>
}
