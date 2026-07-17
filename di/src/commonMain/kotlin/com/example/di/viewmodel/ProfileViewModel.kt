package com.example.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.PredictionRepository
import com.example.domain.repository.SecondRepository
import com.example.domain.repository.KeepTabRepository
import com.example.domain.usecase.keeptab.ToggleKeepTabUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    object Guest : ProfileUiState
    data class Loaded(
        val session: Session.Authenticated,
        val stats: ProfileStats,
        val predictions: List<Prediction>,
        val seconds: List<Second>,
        val keepTabs: List<KeepTab>
    ) : ProfileUiState
    object Error : ProfileUiState
}

data class ProfileStats(
    val reputation: Int,
    val accuracy: Int,
    val totalSeconds: Int,
    val correctSeconds: Int,
    val isAccuracyEligible: Boolean
)

class ProfileViewModel(
    val authRepository: AuthRepository,
    val toggleKeepTab: ToggleKeepTabUseCase,
    val keepTabRepository: KeepTabRepository,
    private val predictionRepository: PredictionRepository,
    private val secondRepository: SecondRepository
) : ViewModel() {

    private val _targetUid = MutableStateFlow<String?>(null)
    val targetUid = _targetUid.asStateFlow()

    val isCurrentUser: StateFlow<Boolean> = combine(
        authRepository.observeSession(),
        _targetUid
    ) { session, targetId ->
        targetId == null || (session as? Session.Authenticated)?.uid == targetId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isTracking: StateFlow<Boolean> = combine(
        authRepository.observeSession(),
        _targetUid
    ) { session, targetId ->
        if (session is Session.Authenticated && targetId != null) {
            keepTabRepository.isTracking(session.uid, targetId).first()
        } else false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.observeSession(),
        predictionRepository.observePredictions(),
        secondRepository.observeSeconds(),
        _targetUid
    ) { session, allPredictions, allSeconds, targetId ->
        if (targetId == null && session is Session.Guest) {
            ProfileUiState.Guest
        } else {
            val user = when {
                targetId != null -> {
                    // If viewing another profile, mock/simulate that authenticated user structure
                    Session.Authenticated(
                        uid = targetId,
                        email = "predictor@eugene.app",
                        name = "Top Predictor ${targetId.take(4)}",
                        handle = "predictor_${targetId.take(4)}",
                        avatarUrl = "https://example.com/avatar/$targetId.png",
                        accuracy = 84,
                        reputation = 1250,
                        resolvedPredictionCount = 12
                    )
                }
                session is Session.Authenticated -> session
                else -> null
            }

            if (user == null) {
                ProfileUiState.Loading
            } else {
                val userSeconds = allSeconds.filter { it.id.startsWith("user") || user.uid == "my_uid" || it.predictionId.isNotBlank() } // simulate/get user's seconds
                val userPredictions = allPredictions.filter { it.createdBy == user.uid }
                
                // Stats calculations
                val totalSeconds = userSeconds.size
                val correctSeconds = userSeconds.count { it.status == SecondStatus.CORRECT }
                val isEligible = user.resolvedPredictionCount >= 5

                val stats = ProfileStats(
                    reputation = user.reputation,
                    accuracy = user.accuracy,
                    totalSeconds = totalSeconds,
                    correctSeconds = correctSeconds,
                    isAccuracyEligible = isEligible
                )

                // Keep list
                val keepTabs = keepTabRepository.observeTracked(user.uid).firstOrNull() ?: emptyList()

                ProfileUiState.Loaded(
                    session = user,
                    stats = stats,
                    predictions = userPredictions,
                    seconds = userSeconds,
                    keepTabs = keepTabs
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState.Loading)

    fun setTargetUid(uid: String?) {
        _targetUid.value = uid
    }

    fun signInAs(type: String) {
        viewModelScope.launch {
            val email = when (type) {
                "NEW" -> "new@example.com"
                else -> "established@example.com"
            }
            authRepository.signInWithEmail(email, "password")
        }
    }

    fun toggleKeepForTarget() {
        val targetId = _targetUid.value ?: return
        viewModelScope.launch {
            toggleKeepTab(targetId)
        }
    }

    fun toggleKeep(uid: String) {
        viewModelScope.launch {
            toggleKeepTab(uid)
        }
    }
}
