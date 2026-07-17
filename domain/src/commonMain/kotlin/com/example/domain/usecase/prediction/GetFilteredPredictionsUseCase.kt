package com.example.domain.usecase.prediction

import com.example.domain.model.*
import com.example.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFilteredPredictionsUseCase(
    private val predictionRepository: PredictionRepository
) {
    operator fun invoke(
        query: String = "",
        category: PredictionCategory? = null,
        status: PredictionStatus? = null
    ): Flow<List<Prediction>> {
        return predictionRepository.observePredictions().map { list ->
            list.filter { prediction ->
                val matchesQuery = if (query.isNotBlank()) {
                    prediction.title.contains(query, ignoreCase = true) ||
                    prediction.description.contains(query, ignoreCase = true) ||
                    prediction.rulesDescription.contains(query, ignoreCase = true)
                } else {
                    true
                }

                val matchesCategory = if (category != null) {
                    prediction.category == category
                } else {
                    true
                }

                val matchesStatus = if (status != null) {
                    prediction.status == status
                } else {
                    true
                }

                matchesQuery && matchesCategory && matchesStatus
            }
        }
    }
}
