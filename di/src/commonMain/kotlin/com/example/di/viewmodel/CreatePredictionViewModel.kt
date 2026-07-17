package com.example.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.PredictionCategory
import com.example.domain.usecase.submission.SubmitPredictionUseCase
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

enum class CreateStep {
    TITLE_RULES, CATEGORY, OUTCOMES, DATES, REVIEW
}

enum class SubmissionAnimState {
    IDLE, SUBMITTING, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED
}

class CreatePredictionViewModel(
    private val submitPrediction: SubmitPredictionUseCase,
    val authRepository: AuthRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow(CreateStep.TITLE_RULES)
    val currentStep = _currentStep.asStateFlow()

    val title = MutableStateFlow("")
    val rules = MutableStateFlow("")
    val category = MutableStateFlow<PredictionCategory?>(null)
    val outcomes = MutableStateFlow(listOf("", ""))
    
    // Default dates: closes in 3 days, resolves in 4 days
    private val defaultClose = Clock.System.now() + 3.days
    private val defaultResolve = Clock.System.now() + 4.days
    val closesAt = MutableStateFlow(defaultClose)
    val resolvesAt = MutableStateFlow(defaultResolve)
    
    val source = MutableStateFlow("Official Statistics / Public Records")
    val criteria = MutableStateFlow("Will be verified based on final official announcements.")

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError = _validationError.asStateFlow()

    private val _submissionState = MutableStateFlow(SubmissionAnimState.IDLE)
    val submissionState = _submissionState.asStateFlow()

    fun setStep(step: CreateStep) {
        _currentStep.value = step
    }

    fun addOutcome() {
        if (outcomes.value.size < 6) {
            outcomes.value = outcomes.value + ""
        }
    }

    fun removeOutcome(index: Int) {
        if (outcomes.value.size > 2) {
            outcomes.value = outcomes.value.toMutableList().apply { removeAt(index) }
        }
    }

    fun updateOutcome(index: Int, text: String) {
        outcomes.value = outcomes.value.toMutableList().apply { this[index] = text }
    }

    fun validateCurrentStep(): Boolean {
        _validationError.value = null
        when (_currentStep.value) {
            CreateStep.TITLE_RULES -> {
                if (title.value.isBlank()) {
                    _validationError.value = "Question cannot be blank"
                    return false
                }
                if (title.value.length > 120) {
                    _validationError.value = "Question must be 120 characters or less"
                    return false
                }
                if (rules.value.length > 300) {
                    _validationError.value = "Rules must be 300 characters or less"
                    return false
                }
            }
            CreateStep.CATEGORY -> {
                if (category.value == null) {
                    _validationError.value = "Please select a category"
                    return false
                }
            }
            CreateStep.OUTCOMES -> {
                val nonBlank = outcomes.value.filter { it.isNotBlank() }
                if (nonBlank.size < 2) {
                    _validationError.value = "Please enter at least 2 outcomes"
                    return false
                }
                if (nonBlank.distinct().size != nonBlank.size) {
                    _validationError.value = "Outcomes must be unique"
                    return false
                }
            }
            CreateStep.DATES -> {
                val now = Clock.System.now()
                if (closesAt.value <= now) {
                    _validationError.value = "Close date must be in the future"
                    return false
                }
                if (resolvesAt.value <= closesAt.value) {
                    _validationError.value = "Resolution date must be strictly after close date"
                    return false
                }
            }
            CreateStep.REVIEW -> {}
        }
        return true
    }

    fun nextStep(): Boolean {
        if (validateCurrentStep()) {
            val steps = CreateStep.entries
            val currentIndex = steps.indexOf(_currentStep.value)
            if (currentIndex < steps.size - 1) {
                _currentStep.value = steps[currentIndex + 1]
            }
            return true
        }
        return false
    }

    fun prevStep() {
        val steps = CreateStep.entries
        val currentIndex = steps.indexOf(_currentStep.value)
        if (currentIndex > 0) {
            _currentStep.value = steps[currentIndex - 1]
        }
    }

    fun submit() {
        if (!validateCurrentStep()) return
        
        viewModelScope.launch {
            _submissionState.value = SubmissionAnimState.SUBMITTING
            
            val result = submitPrediction(
                title = title.value,
                category = category.value ?: PredictionCategory.POLITICS,
                closesAt = closesAt.value,
                resolvesAt = resolvesAt.value,
                source = source.value,
                criteria = criteria.value,
                outcomes = outcomes.value.filter { it.isNotBlank() }
            )

            result.fold(
                onSuccess = {
                    // Beautiful choreographed simulation transition
                    _submissionState.value = SubmissionAnimState.SUBMITTED
                    delay(1500)
                    _submissionState.value = SubmissionAnimState.UNDER_REVIEW
                    delay(2000)
                    // Auto approve for smooth flow
                    _submissionState.value = SubmissionAnimState.APPROVED
                },
                onFailure = {
                    _validationError.value = it.message ?: "Failed to submit prediction"
                    _submissionState.value = SubmissionAnimState.REJECTED
                }
            )
        }
    }

    fun reset() {
        _currentStep.value = CreateStep.TITLE_RULES
        title.value = ""
        rules.value = ""
        category.value = null
        outcomes.value = listOf("", "")
        closesAt.value = Clock.System.now() + 3.days
        resolvesAt.value = Clock.System.now() + 4.days
        _validationError.value = null
        _submissionState.value = SubmissionAnimState.IDLE
    }
}
