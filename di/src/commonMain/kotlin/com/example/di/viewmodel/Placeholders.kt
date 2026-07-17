package com.example.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.domain.usecase.keeptab.ToggleKeepTabUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ==========================================
// LEADERBOARD VIEW MODEL
// ==========================================

data class LeaderboardEntry(
    val uid: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val accuracy: Int,
    val reputation: Int,
    val resolvedCount: Int,
    val isTracking: Boolean,
    val rank: Int? // null if ineligible (< 5 resolved)
)

class LeaderboardViewModel(
    val authRepository: AuthRepository,
    val keepTabRepository: KeepTabRepository,
    val toggleKeepTab: ToggleKeepTabUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<PredictionCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow("All-Time") // "Weekly", "Monthly", "All-Time"
    val selectedTimeFilter = _selectedTimeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Base mock predictors including seed users and additional entries
    private val basePredictors = listOf(
        LeaderboardEntry("user_g", "Grace Guru", "@grace_g", "https://images.unsplash.com/photo-1544005313-94ddf0286df2", 91, 1450, 20, false, null),
        LeaderboardEntry("user_d", "Charlie Crown", "@charlie_c", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d", 88, 980, 15, false, null),
        LeaderboardEntry("user_e", "Diane Defender", "@diane_d", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80", 84, 850, 10, false, null),
        LeaderboardEntry("user_c", "Eugene Fan", "@eugene_fan", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6", 80, 1200, 12, false, null),
        LeaderboardEntry("user_b", "Bob Builder", "@bob_b", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d", 72, 450, 5, false, null),
        LeaderboardEntry("user_f", "Frank Flash", "@frank_f", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e", 68, 320, 6, false, null),
        LeaderboardEntry("user_a", "Alice Adams", "@alice_adams", "https://images.unsplash.com/photo-1494790108377-be9c29b29330", 45, 120, 4, false, null), // Ineligible (< 5 resolved)
        LeaderboardEntry("user_new", "New Predictor", "@new_p", "https://images.unsplash.com/photo-1517841905240-472988babdf9", 0, 0, 0, false, null) // Ineligible
    )

    val session: StateFlow<Session> = authRepository.observeSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Session.Guest)

    val predictors: StateFlow<List<LeaderboardEntry>> = combine(
        session,
        _selectedCategory,
        _selectedTimeFilter,
        _searchQuery,
        session.flatMapLatest { sess ->
            if (sess is Session.Authenticated) keepTabRepository.observeTracked(sess.uid) else flowOf(emptyList())
        }
    ) { sess, category, timeFilter, query, trackedList ->
        val trackedIds = trackedList.map { it.trackedUid }.toSet()

        // Apply filters & search queries
        val filtered = basePredictors.filter { entry ->
            val matchesQuery = query.isEmpty() || entry.name.contains(query, ignoreCase = true) || entry.handle.contains(query, ignoreCase = true)
            matchesQuery
        }.map { entry ->
            // Update tracking state reactively
            entry.copy(isTracking = trackedIds.contains(entry.uid))
        }

        // Rank the eligible predictors (resolvedCount >= 5) by accuracy descending
        val eligible = filtered.filter { it.resolvedCount >= 5 }
            .sortedByDescending { it.accuracy }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

        val ineligible = filtered.filter { it.resolvedCount < 5 }
            .map { entry -> entry.copy(rank = null) }

        eligible + ineligible
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: PredictionCategory?) {
        _selectedCategory.value = category
    }

    fun selectTimeFilter(filter: String) {
        _selectedTimeFilter.value = filter
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun toggleTracking(uid: String) {
        viewModelScope.launch {
            toggleKeepTab(uid)
        }
    }
}

// ==========================================
// SETTINGS VIEW MODEL
// ==========================================

class SettingsViewModel(
    val preferencesRepository: PreferencesRepository,
    val authRepository: AuthRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = preferencesRepository.observeDarkTheme()
        .map { it ?: true } // Default to dark theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val session: StateFlow<Session> = authRepository.observeSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Session.Guest)

    fun toggleDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(isDark)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            // Delete account action (clears credentials and signs out)
            authRepository.signOut()
        }
    }
}

// ==========================================
// NOTIFICATIONS VIEW MODEL
// ==========================================

enum class NotificationCategoryTab { ALL, ACTIVITY, MODERATION }

enum class NotificationType {
    PREDICTION_APPROVED,
    PREDICTION_REJECTED,
    PREDICTION_LIVE,
    PREDICTION_CLOSES_SOON,
    PREDICTION_RESOLVED,
    PREDICTION_VOIDED,
    FLAGGED_REASONING,
    CONTENT_REMOVED,
    ACCOUNT_LIMITED,
    TRACKED_ACTIVITY
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: String, // "Today", "Yesterday", "Older"
    val isRead: Boolean = false,
    val predictionId: String? = null
)

class NotificationsViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow(NotificationCategoryTab.ALL)
    val selectedTab = _selectedTab.asStateFlow()

    // 10 mock trigger types matching the required system behaviors and moderation cards
    private val allNotifications = listOf(
        // Today
        NotificationItem(
            id = "notif_1",
            type = NotificationType.PREDICTION_APPROVED,
            title = "Submission Approved",
            message = "Your submitted prediction question 'Will SpaceX launch Starship on a orbital flight?' has been approved by moderators.",
            timestamp = "Today"
        ),
        NotificationItem(
            id = "notif_2",
            type = NotificationType.PREDICTION_RESOLVED,
            title = "Prediction Resolved",
            message = "The prediction 'Most Popular Language on GitHub' has been resolved. Your cast Second was CORRECT! +40 Reputation.",
            timestamp = "Today",
            predictionId = "pred_3_outcomes"
        ),
        NotificationItem(
            id = "notif_3",
            type = NotificationType.FLAGGED_REASONING,
            title = "Reasoning Flagged",
            message = "Your reasoning description on the prediction 'Will the UK rejoin EU single market?' has been flagged for containing off-topic discourse.",
            timestamp = "Today",
            predictionId = "pred_2_outcomes"
        ),
        // Yesterday
        NotificationItem(
            id = "notif_4",
            type = NotificationType.TRACKED_ACTIVITY,
            title = "Predictor Activity",
            message = "@eugene_fan placed a Second on 'Will OpenAI release GPT-5 this year?'",
            timestamp = "Yesterday"
        ),
        NotificationItem(
            id = "notif_5",
            type = NotificationType.PREDICTION_LIVE,
            title = "Prediction Now Live",
            message = "The prediction 'Will Apple announce an AR device?' is now Live and accepting Seconds.",
            timestamp = "Yesterday"
        ),
        NotificationItem(
            id = "notif_6",
            type = NotificationType.PREDICTION_CLOSES_SOON,
            title = "Prediction Closing Soon",
            message = "The prediction 'Will NASA land Artemis 2 by 2026?' closes in less than 2 hours. Lock in your Second now.",
            timestamp = "Yesterday"
        ),
        // Older
        NotificationItem(
            id = "notif_7",
            type = NotificationType.CONTENT_REMOVED,
            title = "Content Removed",
            message = "A Discourse comment you posted was removed by a moderator for violating our community guidelines.",
            timestamp = "Older"
        ),
        NotificationItem(
            id = "notif_8",
            type = NotificationType.ACCOUNT_LIMITED,
            title = "Account Limited",
            message = "Your account has been placed under temporary 48-hour limitation due to multiple flags.",
            timestamp = "Older"
        ),
        NotificationItem(
            id = "notif_9",
            type = NotificationType.PREDICTION_REJECTED,
            title = "Submission Rejected",
            message = "Your submitted prediction 'Will Bitcoin hit $100k by tomorrow?' was rejected: duplicates existing topic.",
            timestamp = "Older"
        ),
        NotificationItem(
            id = "notif_10",
            type = NotificationType.PREDICTION_VOIDED,
            title = "Prediction Voided",
            message = "The prediction question on 'US Elections' was Voided by admins due to unclear resolution criteria. All cast Seconds returned.",
            timestamp = "Older"
        )
    )

    val notifications: StateFlow<List<NotificationItem>> = _selectedTab.map { tab ->
        when (tab) {
            NotificationCategoryTab.ALL -> allNotifications
            NotificationCategoryTab.ACTIVITY -> allNotifications.filter {
                it.type in listOf(
                    NotificationType.PREDICTION_APPROVED,
                    NotificationType.PREDICTION_LIVE,
                    NotificationType.PREDICTION_CLOSES_SOON,
                    NotificationType.PREDICTION_RESOLVED,
                    NotificationType.TRACKED_ACTIVITY
                )
            }
            NotificationCategoryTab.MODERATION -> allNotifications.filter {
                it.type in listOf(
                    NotificationType.PREDICTION_REJECTED,
                    NotificationType.PREDICTION_VOIDED,
                    NotificationType.FLAGGED_REASONING,
                    NotificationType.CONTENT_REMOVED,
                    NotificationType.ACCOUNT_LIMITED
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allNotifications)

    fun selectTab(tab: NotificationCategoryTab) {
        _selectedTab.value = tab
    }
}
