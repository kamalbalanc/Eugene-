package com.example.data.repository

import com.example.data.local.PlatformSettings
import com.example.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PreferencesRepositoryImpl(
    private val platformSettings: PlatformSettings
) : PreferencesRepository {

    private val KEY_DARK_THEME = "dark_theme"
    private val darkThemeFlow = MutableStateFlow(platformSettings.getBoolean(KEY_DARK_THEME))

    override fun observeDarkTheme(): Flow<Boolean?> {
        return darkThemeFlow
    }

    override suspend fun setDarkTheme(isDark: Boolean): Result<Unit> {
        return try {
            platformSettings.setBoolean(KEY_DARK_THEME, isDark)
            darkThemeFlow.value = isDark
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
