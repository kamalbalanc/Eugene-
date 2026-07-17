package com.example.data.repository

import com.example.data.database.dao.SubmissionDao
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toRecord
import com.example.data.remote.EugeneApiService
import com.example.domain.model.ApprovalState
import com.example.domain.model.Submission
import com.example.domain.repository.SubmissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SubmissionRepositoryImpl(
    private val submissionDao: SubmissionDao,
    private val apiService: EugeneApiService
) : SubmissionRepository {

    override fun observeSubmissions(): Flow<List<Submission>> {
        return submissionDao.observeSubmissions().map { records ->
            records.map { it.toDomain() }
        }
    }

    override suspend fun submit(submission: Submission): Result<Unit> {
        return try {
            submissionDao.insert(submission.toRecord())
            
            val payload = mapOf(
                "id" to submission.id,
                "title" to submission.title,
                "category" to submission.category.name,
                "submittedBy" to submission.submittedBy,
                "submittedAt" to submission.submittedAt.toString(),
                "closesAt" to submission.closesAt.toString(),
                "resolvesAt" to submission.resolvesAt.toString(),
                "source" to submission.source,
                "criteria" to submission.criteria,
                "state" to submission.state.name,
                "outcomes" to Json.encodeToString(submission.outcomes)
            )
            
            try {
                apiService.submitPrediction(payload)
            } catch (e: Exception) {
                // Ignore remote exception for true fire-and-forget submission
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateState(submissionId: String, state: ApprovalState): Result<Unit> {
        return try {
            submissionDao.updateState(submissionId, state.name)
            apiService.updateSubmissionState(submissionId, state.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
