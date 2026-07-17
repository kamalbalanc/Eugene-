package com.example.domain

import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.domain.usecase.submission.SubmitPredictionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SubmitPredictionUseCaseTest {

    class FakeAuthRepository(private val session: Session) : AuthRepository {
        override fun observeSession(): Flow<Session> = flowOf(session)
        override suspend fun signInWithEmail(email: String, password: String): Result<Session.Authenticated> = TODO()
        override suspend fun signUpWithEmail(email: String, password: String, name: String, handle: String): Result<Session.Authenticated> = TODO()
        override suspend fun signInWithGoogle(idToken: String): Result<Session.Authenticated> = TODO()
        override suspend fun signOut(): Result<Unit> = TODO()
        override suspend fun updateProfile(name: String, handle: String): Result<Unit> = TODO()
    }

    class FakeSubmissionRepository : SubmissionRepository {
        var submitCalled = false
        override fun observeSubmissions(): Flow<List<Submission>> = TODO()
        override suspend fun submit(submission: Submission): Result<Unit> {
            submitCalled = true
            return Result.success(Unit)
        }
        override suspend fun updateState(submissionId: String, state: ApprovalState): Result<Unit> = TODO()
    }

    private val authenticatedSession = Session.Authenticated(
        uid = "user_123",
        email = "user@example.com",
        name = "User Name",
        handle = "user_handle",
        avatarUrl = "avatar",
        accuracy = 90,
        reputation = 1000,
        resolvedPredictionCount = 6
    )

    @Test
    fun testValidSubmission() = runTest {
        val authRepo = FakeAuthRepository(authenticatedSession)
        val submissionRepo = FakeSubmissionRepository()
        val useCase = SubmitPredictionUseCase(authRepo, submissionRepo)

        val now = Clock.System.now()
        val closesAt = now + 1.days
        val resolvesAt = closesAt + 1.days

        val result = useCase(
            title = "Will it rain tomorrow?",
            category = PredictionCategory.SCIENCE,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            source = "Weather Channel",
            criteria = "More than 1mm rain",
            outcomes = listOf("Yes", "No")
        )

        assertTrue(result.isSuccess)
        assertTrue(submissionRepo.submitCalled)
    }

    @Test
    fun testRejectsSingleOutcome() = runTest {
        val authRepo = FakeAuthRepository(authenticatedSession)
        val submissionRepo = FakeSubmissionRepository()
        val useCase = SubmitPredictionUseCase(authRepo, submissionRepo)

        val now = Clock.System.now()
        val closesAt = now + 1.days
        val resolvesAt = closesAt + 1.days

        val result = useCase(
            title = "Will it rain tomorrow?",
            category = PredictionCategory.SCIENCE,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            source = "Weather Channel",
            criteria = "Criteria",
            outcomes = listOf("Yes") // only 1 outcome
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AppErrorException)
        val error = exception.error
        assertTrue(error is AppError.Validation)
        assertTrue(error.reason.contains("at least 2", ignoreCase = true))
    }

    @Test
    fun testRejectsSevenOutcomes() = runTest {
        val authRepo = FakeAuthRepository(authenticatedSession)
        val submissionRepo = FakeSubmissionRepository()
        val useCase = SubmitPredictionUseCase(authRepo, submissionRepo)

        val now = Clock.System.now()
        val closesAt = now + 1.days
        val resolvesAt = closesAt + 1.days

        val result = useCase(
            title = "Question",
            category = PredictionCategory.SCIENCE,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            source = "Source",
            criteria = "Criteria",
            outcomes = listOf("1", "2", "3", "4", "5", "6", "7") // 7 outcomes
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AppErrorException)
        val error = exception.error
        assertTrue(error is AppError.Validation)
        assertTrue(error.reason.contains("up to 6", ignoreCase = true))
    }

    @Test
    fun testRejectsDuplicateOutcomes() = runTest {
        val authRepo = FakeAuthRepository(authenticatedSession)
        val submissionRepo = FakeSubmissionRepository()
        val useCase = SubmitPredictionUseCase(authRepo, submissionRepo)

        val now = Clock.System.now()
        val closesAt = now + 1.days
        val resolvesAt = closesAt + 1.days

        val result = useCase(
            title = "Question",
            category = PredictionCategory.SCIENCE,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            source = "Source",
            criteria = "Criteria",
            outcomes = listOf("Yes", "Yes") // duplicate
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AppErrorException)
        val error = exception.error
        assertTrue(error is AppError.Validation)
        assertTrue(error.reason.contains("must be different", ignoreCase = true))
    }

    @Test
    fun testRejectsResolveBeforeClose() = runTest {
        val authRepo = FakeAuthRepository(authenticatedSession)
        val submissionRepo = FakeSubmissionRepository()
        val useCase = SubmitPredictionUseCase(authRepo, submissionRepo)

        val now = Clock.System.now()
        val closesAt = now + 1.days
        val resolvesAt = now // resolvesAt is now, closesAt is in 1 day

        val result = useCase(
            title = "Question",
            category = PredictionCategory.SCIENCE,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            source = "Source",
            criteria = "Criteria",
            outcomes = listOf("Yes", "No")
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AppErrorException)
        val error = exception.error
        assertTrue(error is AppError.Validation)
        assertTrue(error.reason.contains("after closesAt", ignoreCase = true))
    }
}
