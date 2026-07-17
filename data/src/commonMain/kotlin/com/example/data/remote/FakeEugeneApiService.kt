package com.example.data.remote

import com.example.data.remote.dto.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

enum class SessionType { GUEST, NEW, ESTABLISHED }

class FakeEugeneApiService(
    private val config: FakeNetworkConfig
) : EugeneApiService {

    // Mutable in-memory store initialized from SeedDataProvider
    var predictions = SeedDataProvider.getDefaultPredictions().toMutableList()
    var comments = SeedDataProvider.getDefaultComments().toMutableList()
    var discourseEntries = SeedDataProvider.getDefaultDiscourse().toMutableList()
    var keepTabs = SeedDataProvider.getDefaultKeepTabs().toMutableList()
    var snapshots = SeedDataProvider.getDefaultSnapshots().toMutableList()
    var submissions = SeedDataProvider.getDefaultSubmissions().toMutableList()
    
    // Auth session state
    var currentSessionType = SessionType.ESTABLISHED

    // Helper to simulate latency and failure scenarios
    private suspend fun simulateLatencyAndFailure(endpointName: String) {
        if (config.isOffline) {
            throw Exception("Offline Simulation: No internet connection.")
        }

        val scenario = config.forcedScenario

        // Determine delay latency
        val latency = when (scenario) {
            FakeScenario.SLOW_SUCCESS -> 4500L
            else -> config.latencyMs
        }
        if (latency > 0) {
            kotlinx.coroutines.delay(latency)
        }

        // Check if session expiry simulation is on
        if (config.isSessionExpired) {
            throw Exception("401 Unauthorized: Session Expired")
        }

        // Trigger failures based on forced scenarios
        if (scenario != null) {
            when (scenario) {
                FakeScenario.NETWORK_ERROR -> throw Exception("Simulated network failure.")
                FakeScenario.SERVER_ERROR -> throw Exception("500 Internal Server Error.")
                FakeScenario.VALIDATION_ERROR -> throw Exception("400 Bad Request: Validation failed on $endpointName.")
                FakeScenario.TRANSIENT_ERROR -> {
                    config.transientAttempts++
                    if (config.transientAttempts <= 2) {
                        throw Exception("Transient error (failure ${config.transientAttempts}/2).")
                    }
                }
                FakeScenario.PERSISTENT_ERROR -> throw Exception("Persistent failure on $endpointName.")
                FakeScenario.RATE_LIMITED -> throw Exception("429 Too Many Requests: Rate limited.")
                else -> { /* SUCCESS, SLOW_SUCCESS, EMPTY_RESULT, CONFLICT_STALE_CLOSE, NO_ROLLUP_YET handled at return site */ }
            }
        } else if (config.failureRate > 0f) {
            if (kotlin.random.Random.nextFloat() < config.failureRate) {
                throw Exception("Random failure triggered by failure rate on $endpointName.")
            }
        }
    }

    // Resetting/Seeding controls
    fun resetToSeedData() {
        predictions = SeedDataProvider.getDefaultPredictions().toMutableList()
        comments = SeedDataProvider.getDefaultComments().toMutableList()
        discourseEntries = SeedDataProvider.getDefaultDiscourse().toMutableList()
        keepTabs = SeedDataProvider.getDefaultKeepTabs().toMutableList()
        snapshots = SeedDataProvider.getDefaultSnapshots().toMutableList()
        submissions = SeedDataProvider.getDefaultSubmissions().toMutableList()
        config.transientAttempts = 0
    }

    fun wipeAllData() {
        predictions.clear()
        comments.clear()
        discourseEntries.clear()
        keepTabs.clear()
        snapshots.clear()
        submissions.clear()
    }

    fun seedEdgeCases() {
        predictions.addAll(SeedDataProvider.getEdgeCasePredictions())
        // Reset transient error counter
        config.transientAttempts = 0
    }

    fun forceSession(type: SessionType) {
        currentSessionType = type
    }

    // --- API Implementations ---

    override suspend fun getPredictions(): List<PredictionDto> {
        simulateLatencyAndFailure("getPredictions")
        if (config.forcedScenario == FakeScenario.EMPTY_RESULT) {
            return emptyList()
        }
        return predictions
    }

    override suspend fun getPrediction(id: String): PredictionDto? {
        simulateLatencyAndFailure("getPrediction")
        if (config.forcedScenario == FakeScenario.EMPTY_RESULT) {
            return null
        }
        return predictions.find { it.id == id }
    }

    override suspend fun castSecond(second: SecondDto) {
        simulateLatencyAndFailure("castSecond")
        
        // Spec: COMPONENT CONFLICT_STALE_CLOSE forced during a Second-cast attempt
        if (config.forcedScenario == FakeScenario.CONFLICT_STALE_CLOSE) {
            throw Exception("CONFLICT_STALE_CLOSE: This prediction closed while you were viewing it.")
        }

        // Simulating immutable second addition (no edits allowed)
        // Find prediction and options to update stats
        val predictionIndex = predictions.indexOfFirst { it.id == second.predictionId }
        if (predictionIndex != -1) {
            val pred = predictions[predictionIndex]
            val updatedOptions = pred.options.map { opt ->
                if (opt.id == second.optionId) {
                    val newSeconds = opt.seconds + 1000 // increment weights
                    opt.copy(seconds = newSeconds)
                } else {
                    opt
                }
            }
            val totalSeconds = updatedOptions.sumOf { it.seconds }
            val recomputedOptions = updatedOptions.map { opt ->
                val percentage = if (totalSeconds > 0) (opt.seconds * 100) / totalSeconds else 0
                opt.copy(percentage = percentage)
            }
            predictions[predictionIndex] = pred.copy(
                options = recomputedOptions,
                totalSeconds = totalSeconds
            )
        }
    }

    override suspend fun getComments(predictionId: String): List<CommentDto> {
        simulateLatencyAndFailure("getComments")
        if (config.forcedScenario == FakeScenario.EMPTY_RESULT) {
            return emptyList()
        }
        return comments.filter { it.predictionId == predictionId }
    }

    override suspend fun addComment(comment: CommentDto) {
        simulateLatencyAndFailure("addComment")
        comments.add(comment)
    }

    override suspend fun getDiscourse(predictionId: String): List<DiscourseDto> {
        simulateLatencyAndFailure("getDiscourse")
        if (config.forcedScenario == FakeScenario.EMPTY_RESULT) {
            return emptyList()
        }
        return discourseEntries.filter { it.predictionId == predictionId }
    }

    override suspend fun postDiscourse(entry: DiscourseDto) {
        simulateLatencyAndFailure("postDiscourse")
        discourseEntries.add(entry)
    }

    override suspend fun markDiscourseHelpful(entryId: String) {
        simulateLatencyAndFailure("markDiscourseHelpful")
        val index = discourseEntries.indexOfFirst { it.id == entryId }
        if (index != -1) {
            val entry = discourseEntries[index]
            discourseEntries[index] = entry.copy(helpfulCount = entry.helpfulCount + 1)
        }
    }

    override suspend fun flagDiscourse(entryId: String) {
        simulateLatencyAndFailure("flagDiscourse")
        val index = discourseEntries.indexOfFirst { it.id == entryId }
        if (index != -1) {
            val entry = discourseEntries[index]
            discourseEntries[index] = entry.copy(flagCount = entry.flagCount + 1)
        }
    }

    override suspend fun getSnapshots(predictionId: String, range: String): List<SecondingSnapshotDto> {
        simulateLatencyAndFailure("getSnapshots")
        if (config.forcedScenario == FakeScenario.EMPTY_RESULT || config.forcedScenario == FakeScenario.NO_ROLLUP_YET) {
            return emptyList()
        }
        return snapshots.filter { it.predictionId == predictionId }
    }

    override suspend fun recordSnapshot(predictionId: String, snapshot: SecondingSnapshotDto) {
        simulateLatencyAndFailure("recordSnapshot")
        snapshots.add(snapshot)
    }

    override suspend fun computeDownsampledRollup(predictionId: String, granularity: String) {
        simulateLatencyAndFailure("computeDownsampledRollup")
        // No-op for fake but simulates successfully
    }

    override suspend fun submitPrediction(submission: Map<String, String>) {
        simulateLatencyAndFailure("submitPrediction")
        val updated = submission.toMutableMap()
        if (!updated.containsKey("id")) {
            updated["id"] = "sub_" + Clock.System.now().toEpochMilliseconds()
        }
        if (!updated.containsKey("state")) {
            updated["state"] = "SUBMITTED"
        }
        submissions.add(updated)
    }

    override suspend fun getSubmissions(): List<Map<String, String>> {
        simulateLatencyAndFailure("getSubmissions")
        if (config.forcedScenario == FakeScenario.EMPTY_RESULT) {
            return emptyList()
        }
        return submissions
    }

    override suspend fun updateSubmissionState(submissionId: String, state: String) {
        simulateLatencyAndFailure("updateSubmissionState")
        val index = submissions.indexOfFirst { it["id"] == submissionId }
        if (index != -1) {
            val sub = submissions[index].toMutableMap()
            sub["state"] = state
            submissions[index] = sub
        }
    }

    override suspend fun submitAppeal(appeal: Map<String, String>) {
        simulateLatencyAndFailure("submitAppeal")
        // Appeals are fire-and-forget in fake
    }

    override suspend fun signInWithEmail(email: String, password: String): UserDto {
        simulateLatencyAndFailure("signInWithEmail")
        return getForcedUserDto()
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String, handle: String): UserDto {
        simulateLatencyAndFailure("signUpWithEmail")
        return UserDto(
            uid = "user_new",
            email = email,
            name = name,
            handle = handle,
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9",
            accuracy = 0,
            reputation = 0,
            resolvedPredictionCount = 0
        )
    }

    override suspend fun signInWithGoogle(idToken: String): UserDto {
        simulateLatencyAndFailure("signInWithGoogle")
        return getForcedUserDto()
    }

    override suspend fun updateProfile(name: String, handle: String) {
        simulateLatencyAndFailure("updateProfile")
    }

    override suspend fun getKeepTabs(trackerUid: String): List<KeepTabDto> {
        simulateLatencyAndFailure("getKeepTabs")
        if (config.forcedScenario == FakeScenario.EMPTY_RESULT) {
            return emptyList()
        }
        return keepTabs.filter { it.trackerUid == trackerUid }
    }

    override suspend fun trackUser(trackerUid: String, trackedUid: String) {
        simulateLatencyAndFailure("trackUser")
        
        // Guard against tracking oneself
        if (trackerUid == trackedUid) {
            throw Exception("Validation Error: Cannot keep tab on yourself.")
        }

        // Add to keepTabs
        if (keepTabs.none { it.trackerUid == trackerUid && it.trackedUid == trackedUid }) {
            keepTabs.add(
                KeepTabDto(
                    id = "tab_${trackerUid}_${trackedUid}",
                    trackerUid = trackerUid,
                    trackedUid = trackedUid,
                    createdAt = Clock.System.now()
                )
            )
        }
    }

    override suspend fun untrackUser(trackerUid: String, trackedUid: String) {
        simulateLatencyAndFailure("untrackUser")
        keepTabs.removeAll { it.trackerUid == trackerUid && it.trackedUid == trackedUid }
    }

    override suspend fun syncKeepTabs() {
        simulateLatencyAndFailure("syncKeepTabs")
    }

    private fun getForcedUserDto(): UserDto {
        return when (currentSessionType) {
            SessionType.GUEST -> throw Exception("401 Unauthorized: Guest cannot request user profile.")
            SessionType.NEW -> SeedDataProvider.userNew
            SessionType.ESTABLISHED -> SeedDataProvider.userEstablished
        }
    }
}
