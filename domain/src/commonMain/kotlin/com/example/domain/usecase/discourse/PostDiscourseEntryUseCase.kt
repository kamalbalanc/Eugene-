package com.example.domain.usecase.discourse

import com.example.domain.model.*
import com.example.domain.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class PostDiscourseEntryUseCase(
    private val authRepository: AuthRepository,
    private val discourseRepository: DiscourseRepository
) {
    suspend operator fun invoke(predictionId: String, text: String, parentId: String? = null): Result<Unit> {
        val session = authRepository.observeSession().first()
        if (session !is Session.Authenticated) {
            return Result.failure(AppErrorException(AppError.NotAuthenticated(action = "post_discourse")))
        }

        if (text.isBlank()) {
            return Result.failure(AppErrorException(AppError.Validation("text", "Content cannot be blank")))
        }

        val entry = DiscourseEntry(
            id = IdGenerator.newId(),
            predictionId = predictionId,
            authorUid = session.uid,
            authorName = session.name,
            authorHandle = session.handle,
            authorAvatarUrl = session.avatarUrl,
            postedAt = Clock.System.now(),
            text = text,
            parentId = parentId
        )

        return discourseRepository.postEntry(entry)
    }
}
