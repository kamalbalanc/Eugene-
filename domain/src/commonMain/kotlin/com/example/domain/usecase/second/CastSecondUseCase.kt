package com.example.domain.usecase.second

import com.example.domain.model.*
import com.example.domain.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class CastSecondUseCase(
    private val authRepository: AuthRepository,
    private val secondRepository: SecondRepository,
    private val commentRepository: CommentRepository,
    private val predictionRepository: PredictionRepository,
    private val recordSnapshot: RecordSecondingSnapshotUseCase
) {
    suspend operator fun invoke(predictionId: String, optionId: String, reasoning: String): Result<Unit> {
        val session = authRepository.observeSession().first()
        if (session !is Session.Authenticated) {
            return Result.failure(AppErrorException(AppError.NotAuthenticated(action = "cast_second")))
        }

        val now = Clock.System.now()
        val second = Second(
            id = IdGenerator.newId(),
            predictionId = predictionId,
            optionId = optionId,
            reasoning = reasoning,
            castAt = now,
            status = SecondStatus.PENDING
        )
        secondRepository.castSecond(second).onFailure { return Result.failure(it) }

        commentRepository.addComment(
            Comment(
                id = IdGenerator.newId(),
                predictionId = predictionId,
                authorUid = session.uid,
                authorName = session.name,
                authorHandle = session.handle,
                authorAvatarUrl = session.avatarUrl,
                secondedOptionId = optionId,
                postedAt = now,
                text = reasoning.ifBlank { "Placed a prediction." }
            )
        )

        // Load the prediction to generate the correct outcome snapshot distribution
        val prediction = predictionRepository.observePrediction(predictionId).first()
        if (prediction != null) {
            val outcomes = prediction.options.map { option ->
                val isSeconded = option.id == optionId
                val newSeconds = if (isSeconded) option.seconds + 1 else option.seconds
                // Keep percentage computation or update it
                val newTotalSeconds = prediction.totalSeconds + 1
                val newPercent = if (newTotalSeconds > 0) {
                    ((newSeconds.toDouble() / newTotalSeconds) * 100).toInt()
                } else {
                    option.percentage
                }
                SecondingSnapshotOutcome(
                    outcomeId = option.id,
                    percentage = newPercent,
                    seconds = newSeconds
                )
            }
            val snapshot = SecondingSnapshot(
                predictionId = predictionId,
                timestamp = now,
                totalSeconds = prediction.totalSeconds + 1,
                outcomes = outcomes
            )
            recordSnapshot(predictionId, snapshot)
        }

        return Result.success(Unit)
    }
}
