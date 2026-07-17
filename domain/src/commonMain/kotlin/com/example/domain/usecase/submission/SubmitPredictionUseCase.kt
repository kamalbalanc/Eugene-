package com.example.domain.usecase.submission

import com.example.domain.model.*
import com.example.domain.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class SubmitPredictionUseCase(
    private val authRepository: AuthRepository,
    private val submissionRepository: SubmissionRepository
) {
    suspend operator fun invoke(
        title: String,
        category: PredictionCategory,
        closesAt: kotlinx.datetime.Instant,
        resolvesAt: kotlinx.datetime.Instant,
        source: String,
        criteria: String,
        outcomes: List<String>
    ): Result<Unit> {
        val session = authRepository.observeSession().first()
        if (session !is Session.Authenticated) {
            return Result.failure(AppErrorException(AppError.NotAuthenticated(action = "submit_prediction")))
        }

        if (title.isBlank()) {
            return Result.failure(AppErrorException(AppError.Validation("title", "Title cannot be blank")))
        }

        if (outcomes.size < 2) {
            return Result.failure(AppErrorException(AppError.Validation("outcomes", "Add at least 2 outcomes")))
        }
        if (outcomes.size > 6) {
            return Result.failure(AppErrorException(AppError.Validation("outcomes", "Predictions can have up to 6 outcomes")))
        }

        if (outcomes.distinct().size != outcomes.size) {
            return Result.failure(AppErrorException(AppError.Validation("outcomes", "Outcomes must be different")))
        }

        val now = Clock.System.now()
        if (closesAt <= now) {
            return Result.failure(AppErrorException(AppError.Validation("closesAt", "Close date must be in the future")))
        }
        if (resolvesAt <= closesAt) {
            return Result.failure(AppErrorException(AppError.Validation("resolvesAt", "resolvesAt must be strictly after closesAt")))
        }

        val submission = Submission(
            id = IdGenerator.newId(),
            title = title,
            category = category,
            submittedBy = session.uid,
            submittedAt = now,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            source = source,
            criteria = criteria,
            state = ApprovalState.SUBMITTED,
            outcomes = outcomes
        )

        return submissionRepository.submit(submission)
    }
}
