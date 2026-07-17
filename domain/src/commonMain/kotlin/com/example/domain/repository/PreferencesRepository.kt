package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeDarkTheme(): Flow<Boolean?>
    suspend fun setDarkTheme(isDark: Boolean): Result<Unit>
}
