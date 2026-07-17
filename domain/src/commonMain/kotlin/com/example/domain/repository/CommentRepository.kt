package com.example.domain.repository

import com.example.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun observeComments(predictionId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment): Result<Unit>
}
