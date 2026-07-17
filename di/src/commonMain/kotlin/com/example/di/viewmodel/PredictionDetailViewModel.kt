package com.example.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.usecase.prediction.GetNotableSecondersUseCase
import com.example.domain.usecase.second.CastSecondUseCase
import com.example.domain.usecase.discourse.PostDiscourseEntryUseCase
import com.example.domain.usecase.keeptab.ToggleKeepTabUseCase
import com.example.domain.repository.PredictionRepository
import com.example.domain.repository.CommentRepository
import com.example.domain.repository.DiscourseRepository
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.SecondingSnapshotRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

sealed interface PredictionDetailUiState {
    object Loading : PredictionDetailUiState
    data class Success(
        val prediction: Prediction,
        val session: Session,
        val notableSeconders: List<PredictorSummary>,
        val comments: List<Comment>,
        val discourse: List<DiscourseEntry>
    ) : PredictionDetailUiState
    data class Error(val message: String) : PredictionDetailUiState
}

enum class DetailTab {
    TIMELINE, REASONING, DISCOURSE
}

class PredictionDetailViewModel(
    val getNotableSeconders: GetNotableSecondersUseCase,
    val castSecond: CastSecondUseCase,
    val predictionRepository: PredictionRepository,
    val commentRepository: CommentRepository,
    val discourseRepository: DiscourseRepository,
    val authRepository: AuthRepository,
    val toggleKeepTab: ToggleKeepTabUseCase,
    val snapshotRepository: SecondingSnapshotRepository
) : ViewModel() {

    private val _predictionId = MutableStateFlow<String?>(null)
    val predictionId = _predictionId.asStateFlow()

    private val _currentTab = MutableStateFlow(DetailTab.TIMELINE)
    val currentTab = _currentTab.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL)
    val selectedTimeRange = _selectedTimeRange.asStateFlow()

    private val _isCasting = MutableStateFlow(false)
    val isCasting = _isCasting.asStateFlow()

    private val _castError = MutableStateFlow<String?>(null)
    val castError = _castError.asStateFlow()

    val snapshots: StateFlow<List<SecondingSnapshot>> = combine(
        _predictionId,
        _selectedTimeRange
    ) { id, range ->
        id to range
    }.flatMapLatest { (id, range) ->
        if (id == null) {
            flowOf(emptyList())
        } else {
            viewModelScope.launch {
                snapshotRepository.sync(id)
            }
            snapshotRepository.observeSnapshots(id, range)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<PredictionDetailUiState> = _predictionId.flatMapLatest { id ->
        if (id == null) {
            flowOf(PredictionDetailUiState.Loading)
        } else {
            combine(
                predictionRepository.observePrediction(id),
                authRepository.observeSession(),
                getNotableSeconders(id),
                commentRepository.observeComments(id),
                discourseRepository.observeDiscourse(id)
            ) { prediction, session, notable, comments, discourse ->
                if (prediction == null) {
                    PredictionDetailUiState.Error("Prediction not found")
                } else {
                    PredictionDetailUiState.Success(
                        prediction = prediction,
                        session = session,
                        notableSeconders = notable,
                        comments = comments.sortedByDescending { it.postedAt },
                        discourse = discourse.sortedBy { it.postedAt }
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictionDetailUiState.Loading)

    fun setPredictionId(id: String) {
        _predictionId.value = id
    }

    fun selectTab(tab: DetailTab) {
        _currentTab.value = tab
    }

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun submitSecond(optionId: String, reasoning: String, onSuccess: () -> Unit) {
        val id = _predictionId.value ?: return
        viewModelScope.launch {
            _isCasting.value = true
            _castError.value = null
            castSecond(id, optionId, reasoning).fold(
                onSuccess = {
                    _isCasting.value = false
                    onSuccess()
                },
                onFailure = {
                    _castError.value = it.message ?: "Failed to place Second"
                    _isCasting.value = false
                }
            )
        }
    }

    fun postDiscourseMessage(text: String) {
        val predId = _predictionId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val session = authRepository.observeSession().first()
            if (session is Session.Authenticated) {
                val entry = DiscourseEntry(
                    id = IdGenerator.newId(),
                    predictionId = predId,
                    authorUid = session.uid,
                    authorName = session.name,
                    authorHandle = session.handle,
                    authorAvatarUrl = session.avatarUrl,
                    postedAt = Clock.System.now(),
                    text = text,
                    parentId = null
                )
                discourseRepository.postEntry(entry)
            }
        }
    }

    fun toggleDiscourseHelpful(entryId: String) {
        viewModelScope.launch {
            discourseRepository.markHelpful(entryId)
        }
    }

    fun flagDiscourse(entryId: String) {
        viewModelScope.launch {
            discourseRepository.flag(entryId)
        }
    }

    fun toggleTracker(uid: String) {
        viewModelScope.launch {
            toggleKeepTab(uid)
        }
    }
}
