package com.example.domain

import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.domain.usecase.keeptab.ToggleKeepTabUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ToggleKeepTabUseCaseTest {

    class FakeAuthRepository(private val session: Session) : AuthRepository {
        override fun observeSession(): Flow<Session> = flowOf(session)
        override suspend fun signInWithEmail(email: String, password: String): Result<Session.Authenticated> = TODO()
        override suspend fun signUpWithEmail(email: String, password: String, name: String, handle: String): Result<Session.Authenticated> = TODO()
        override suspend fun signInWithGoogle(idToken: String): Result<Session.Authenticated> = TODO()
        override suspend fun signOut(): Result<Unit> = TODO()
        override suspend fun updateProfile(name: String, handle: String): Result<Unit> = TODO()
    }

    class FakeKeepTabRepository(private val alreadyTracking: Boolean) : KeepTabRepository {
        var trackCalled = false
        var untrackCalled = false

        override fun observeTracked(trackerUid: String): Flow<List<KeepTab>> = TODO()
        override fun isTracking(trackerUid: String, trackedUid: String): Flow<Boolean> = flowOf(alreadyTracking)
        override suspend fun track(trackerUid: String, trackedUid: String): Result<Unit> {
            trackCalled = true
            return Result.success(Unit)
        }
        override suspend fun untrack(trackerUid: String, trackedUid: String): Result<Unit> {
            untrackCalled = true
            return Result.success(Unit)
        }
        override suspend fun sync(): Result<Unit> = TODO()
    }

    @Test
    fun testRejectSelfTracking() = runTest {
        val userSession = Session.Authenticated(
            uid = "user_123",
            email = "user@example.com",
            name = "User Name",
            handle = "user_handle",
            avatarUrl = "avatar",
            accuracy = 90,
            reputation = 1000,
            resolvedPredictionCount = 6
        )
        val authRepo = FakeAuthRepository(userSession)
        val keepTabRepo = FakeKeepTabRepository(alreadyTracking = false)
        val useCase = ToggleKeepTabUseCase(authRepo, keepTabRepo)

        val result = useCase("user_123") // self tracking
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AppErrorException)
        val appError = exception.error
        assertTrue(appError is AppError.Validation)
        assertTrue(appError.reason.contains("yourself", ignoreCase = true))
    }

    @Test
    fun testTrackOtherPredictor() = runTest {
        val userSession = Session.Authenticated(
            uid = "user_123",
            email = "user@example.com",
            name = "User Name",
            handle = "user_handle",
            avatarUrl = "avatar",
            accuracy = 90,
            reputation = 1000,
            resolvedPredictionCount = 6
        )
        val authRepo = FakeAuthRepository(userSession)
        val keepTabRepo = FakeKeepTabRepository(alreadyTracking = false)
        val useCase = ToggleKeepTabUseCase(authRepo, keepTabRepo)

        val result = useCase("other_predictor")
        assertTrue(result.isSuccess)
        assertTrue(keepTabRepo.trackCalled)
        assertFalse(keepTabRepo.untrackCalled)
    }

    @Test
    fun testUntrackOtherPredictor() = runTest {
        val userSession = Session.Authenticated(
            uid = "user_123",
            email = "user@example.com",
            name = "User Name",
            handle = "user_handle",
            avatarUrl = "avatar",
            accuracy = 90,
            reputation = 1000,
            resolvedPredictionCount = 6
        )
        val authRepo = FakeAuthRepository(userSession)
        val keepTabRepo = FakeKeepTabRepository(alreadyTracking = true)
        val useCase = ToggleKeepTabUseCase(authRepo, keepTabRepo)

        val result = useCase("other_predictor")
        assertTrue(result.isSuccess)
        assertFalse(keepTabRepo.trackCalled)
        assertTrue(keepTabRepo.untrackCalled)
    }
}
