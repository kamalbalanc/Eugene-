package com.example.domain.usecase.keeptab

import com.example.domain.model.*
import com.example.domain.repository.*
import kotlinx.coroutines.flow.first

class ToggleKeepTabUseCase(
    private val authRepository: AuthRepository,
    private val keepTabRepository: KeepTabRepository
) {
    suspend operator fun invoke(trackedUid: String): Result<Unit> {
        val session = authRepository.observeSession().first()
        if (session !is Session.Authenticated) {
            return Result.failure(AppErrorException(AppError.NotAuthenticated(action = "keep_tab")))
        }
        if (session.uid == trackedUid) {
            return Result.failure(AppErrorException(AppError.Validation("trackedUid", "You cannot keep tab on yourself")))
        }

        val alreadyTracking = keepTabRepository.isTracking(session.uid, trackedUid).first()
        return if (alreadyTracking) {
            keepTabRepository.untrack(session.uid, trackedUid)
        } else {
            keepTabRepository.track(session.uid, trackedUid)
        }
    }
}
