package com.example.data.repository

import com.example.data.database.dao.CommentDao
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toRecord
import com.example.data.remote.EugeneApiService
import com.example.data.remote.mapper.toDto
import com.example.domain.model.Comment
import com.example.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CommentRepositoryImpl(
    private val commentDao: CommentDao,
    private val apiService: EugeneApiService
) : CommentRepository {

    override fun observeComments(predictionId: String): Flow<List<Comment>> {
        return commentDao.observeComments(predictionId).map { records ->
            records.map { it.toDomain() }
        }
    }

    override suspend fun addComment(comment: Comment): Result<Unit> {
        return try {
            commentDao.insert(comment.toRecord())
            apiService.addComment(comment.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
