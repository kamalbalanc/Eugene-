package com.example.domain.usecase.prediction

import com.example.domain.model.*
import com.example.domain.repository.SecondRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNotableSecondersUseCase(
    private val secondRepository: SecondRepository
) {
    operator fun invoke(predictionId: String): Flow<List<PredictorSummary>> {
        return secondRepository.observeSeconds().map { allSeconds ->
            val predictionSeconds = allSeconds.filter { it.predictionId == predictionId }
            predictionSeconds.map { second ->
                PredictorSummary(
                    uid = "user_" + second.id.take(6),
                    avatarUrl = "https://example.com/avatar/${second.id}.png",
                    accuracyRate = second.crowdPercentAtSecond ?: 80,
                    resolvedPredictionCount = 12
                )
            }.distinctBy { it.uid }
             .sortedByDescending { it.accuracyRate }
             .take(4)
        }
    }
}
