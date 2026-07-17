package com.example.data.remote

import com.example.data.remote.dto.*

interface EugeneApiService {
    suspend fun getPredictions(): List<PredictionDto>
    suspend fun getPrediction(id: String): PredictionDto?
    suspend fun castSecond(second: SecondDto): Unit
    suspend fun getComments(predictionId: String): List<CommentDto>
    suspend fun addComment(comment: CommentDto): Unit
    suspend fun getDiscourse(predictionId: String): List<DiscourseDto>
    suspend fun postDiscourse(entry: DiscourseDto): Unit
    suspend fun markDiscourseHelpful(entryId: String): Unit
    suspend fun flagDiscourse(entryId: String): Unit
    suspend fun getSnapshots(predictionId: String, range: String): List<SecondingSnapshotDto>
    suspend fun recordSnapshot(predictionId: String, snapshot: SecondingSnapshotDto): Unit
    suspend fun computeDownsampledRollup(predictionId: String, granularity: String): Unit
    
    // Fire-and-forget submissions & appeals (plain maps per spec)
    suspend fun submitPrediction(submission: Map<String, String>): Unit
    suspend fun getSubmissions(): List<Map<String, String>>
    suspend fun updateSubmissionState(submissionId: String, state: String): Unit
    suspend fun submitAppeal(appeal: Map<String, String>): Unit
    
    // Auth & Profile
    suspend fun signInWithEmail(email: String, password: String): UserDto
    suspend fun signUpWithEmail(email: String, password: String, name: String, handle: String): UserDto
    suspend fun signInWithGoogle(idToken: String): UserDto
    suspend fun updateProfile(name: String, handle: String): Unit
    
    // Keep Tab
    suspend fun getKeepTabs(trackerUid: String): List<KeepTabDto>
    suspend fun trackUser(trackerUid: String, trackedUid: String): Unit
    suspend fun untrackUser(trackerUid: String, trackedUid: String): Unit
    suspend fun syncKeepTabs(): Unit
}
