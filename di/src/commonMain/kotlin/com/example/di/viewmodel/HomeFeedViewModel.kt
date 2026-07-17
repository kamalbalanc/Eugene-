package com.example.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Prediction
import com.example.domain.model.PredictionStatus
import com.example.domain.model.Session
import com.example.domain.usecase.prediction.GetFilteredPredictionsUseCase
import com.example.domain.repository.PredictionRepository
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeFeedUiState {
    object Loading : HomeFeedUiState
    data class Loaded(
        val predictions: List<Prediction>,
        val featured: List<Prediction>,
        val movingNow: List<Prediction>
    ) : HomeFeedUiState
    data class Error(val message: String) : HomeFeedUiState
    object Empty : HomeFeedUiState
}

enum class HomeFeedTab {
    FOR_YOU, ACTIVITY
}

class HomeFeedViewModel(
    val getFilteredPredictions: GetFilteredPredictionsUseCase,
    val authRepository: AuthRepository,
    private val predictionRepository: PredictionRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(HomeFeedTab.FOR_YOU)
    val currentTab = _currentTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    val session: StateFlow<Session> = authRepository.observeSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Session.Guest)

    val uiState: StateFlow<HomeFeedUiState> = combine(
        getFilteredPredictions(status = PredictionStatus.LIVE),
        _isRefreshing,
        _errorMessage
    ) { predictions, refreshing, errorMsg ->
        if (errorMsg != null) {
            HomeFeedUiState.Error(errorMsg)
        } else if (predictions.isEmpty() && !refreshing) {
            HomeFeedUiState.Empty
        } else if (predictions.isEmpty() && refreshing) {
            HomeFeedUiState.Loading
        } else {
            // Featured: top 3 predictions based on totalSeconds
            val featuredList = predictions.sortedByDescending { it.totalSeconds }.take(3)
            // Moving now: top 4 sorted by totalSeconds
            val movingList = predictions.sortedByDescending { it.totalSeconds }.take(4)
            HomeFeedUiState.Loaded(
                predictions = predictions,
                featured = featuredList,
                movingNow = movingList
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeFeedUiState.Loading)

    init {
        pullToRefresh()
    }

    fun selectTab(tab: HomeFeedTab) {
        _currentTab.value = tab
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            predictionRepository.sync()
                .onFailure {
                    // Fail silently or set error message if needed
                }
            _isRefreshing.value = false
        }
    }
}
