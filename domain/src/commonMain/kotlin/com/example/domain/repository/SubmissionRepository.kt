package com.example.domain.repository

import com.example.domain.model.Submission
import com.example.domain.model.ApprovalState
import kotlinx.coroutines.flow.Flow

interface SubmissionRepository {
    fun observeSubmissions(): Flow<List<Submission>>
    suspend fun submit(submission: Submission): Result<Unit>
    suspend fun updateState(submissionId: String, state: ApprovalState): Result<Unit>
}
