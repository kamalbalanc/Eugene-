package com.example.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Prediction
import com.example.domain.model.PredictionCategory
import com.example.domain.model.PredictionStatus
import com.example.domain.usecase.prediction.GetFilteredPredictionsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ExploreUiState {
    data class Default(
        val trending: List<Prediction>,
        val categories: List<PredictionCategory>
    ) : ExploreUiState
    object SearchActive : ExploreUiState
    data class SearchResults(
        val predictions: List<Prediction>,
        val query: String
    ) : ExploreUiState
    object Loading : ExploreUiState
    object Error : ExploreUiState
}

enum class SortOption {
    MOST_ACTIVE, ENDING_SOON, MOST_SECONDS, NEWEST, RECENTLY_CREATED
}

enum class TimeRangeOption {
    ALL_TIME, PAST_24_HOURS, PAST_7_DAYS, PAST_30_DAYS
}

@OptIn(FlowPreview::class)
class ExploreViewModel(
    private val getFilteredPredictions: GetFilteredPredictionsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PredictionCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _tempSelectedCategories = MutableStateFlow<Set<PredictionCategory>>(emptySet())
    val tempSelectedCategories = _tempSelectedCategories.asStateFlow()

    private val _appliedCategories = MutableStateFlow<Set<PredictionCategory>>(emptySet())
    val appliedCategories = _appliedCategories.asStateFlow()

    private val _tempSortBy = MutableStateFlow(SortOption.MOST_ACTIVE)
    val tempSortBy = _tempSortBy.asStateFlow()

    private val _appliedSortBy = MutableStateFlow(SortOption.MOST_ACTIVE)
    val appliedSortBy = _appliedSortBy.asStateFlow()

    private val _tempTimeRange = MutableStateFlow(TimeRangeOption.ALL_TIME)
    val tempTimeRange = _tempTimeRange.asStateFlow()

    private val _appliedTimeRange = MutableStateFlow(TimeRangeOption.ALL_TIME)
    val appliedTimeRange = _appliedTimeRange.asStateFlow()

    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    val uiState: StateFlow<ExploreUiState> = combine(
        _isSearchActive,
        debouncedQuery,
        _selectedCategory,
        _appliedCategories,
        _appliedSortBy,
        _appliedTimeRange,
        getFilteredPredictions(status = PredictionStatus.LIVE)
    ) { array ->
        val active = array[0] as Boolean
        val query = array[1] as String
        val cat = array[2] as PredictionCategory?
        val appCats = array[3] as Set<PredictionCategory>
        val sortBy = array[4] as SortOption
        val timeRange = array[5] as TimeRangeOption
        val allLive = array[6] as List<Prediction>

        if (active) {
            if (query.isBlank() && cat == null && appCats.isEmpty()) {
                ExploreUiState.SearchActive
            } else {
                var filtered = allLive.filter { p ->
                    val matchesQuery = if (query.isNotBlank()) {
                        p.title.contains(query, ignoreCase = true) ||
                        p.description.contains(query, ignoreCase = true)
                    } else true

                    val matchesCategory = if (cat != null) {
                        p.category == cat
                    } else if (appCats.isNotEmpty()) {
                        appCats.contains(p.category)
                    } else true

                    matchesQuery && matchesCategory
                }

                // Sorting
                filtered = when (sortBy) {
                    SortOption.MOST_ACTIVE -> filtered.sortedByDescending { it.totalSeconds }
                    SortOption.ENDING_SOON -> filtered.sortedBy { it.closesAt }
                    SortOption.MOST_SECONDS -> filtered.sortedByDescending { it.totalSeconds }
                    SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
                    SortOption.RECENTLY_CREATED -> filtered.sortedByDescending { it.createdAt }
                }

                ExploreUiState.SearchResults(predictions = filtered, query = query)
            }
        } else {
            // Default view: Show trending (most active)
            val trending = allLive.sortedByDescending { it.totalSeconds }.take(5)
            ExploreUiState.Default(
                trending = trending,
                categories = PredictionCategory.entries
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExploreUiState.Loading)

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            _selectedCategory.value = null
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: PredictionCategory?) {
        _selectedCategory.value = category
        if (category != null) {
            _isSearchActive.value = true
        }
    }

    // Filters Sheet Actions
    fun toggleTempCategory(category: PredictionCategory) {
        val current = _tempSelectedCategories.value
        _tempSelectedCategories.value = if (current.contains(category)) {
            current - category
        } else {
            current + category
        }
    }

    fun setTempSortBy(option: SortOption) {
        _tempSortBy.value = option
    }

    fun setTempTimeRange(option: TimeRangeOption) {
        _tempTimeRange.value = option
    }

    fun applyFilters() {
        _appliedCategories.value = _tempSelectedCategories.value
        _appliedSortBy.value = _tempSortBy.value
        _appliedTimeRange.value = _tempTimeRange.value
        _isSearchActive.value = true
    }

    fun resetFilters() {
        _tempSelectedCategories.value = emptySet()
        _tempSortBy.value = SortOption.MOST_ACTIVE
        _tempTimeRange.value = TimeRangeOption.ALL_TIME
        _appliedCategories.value = emptySet()
        _appliedSortBy.value = SortOption.MOST_ACTIVE
        _appliedTimeRange.value = TimeRangeOption.ALL_TIME
    }
}
