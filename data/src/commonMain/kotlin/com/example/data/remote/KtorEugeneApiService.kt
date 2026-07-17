package com.example.data.remote

import com.example.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorEugeneApiService(
    private val client: HttpClient,
    private val baseUrl: String = "https://api.eugene.example.com"
) : EugeneApiService {

    override suspend fun getPredictions(): List<PredictionDto> {
        return client.get("$baseUrl/predictions").body()
    }

    override suspend fun getPrediction(id: String): PredictionDto? {
        val response = client.get("$baseUrl/predictions/$id")
        return if (response.status == HttpStatusCode.NotFound) {
            null
        } else {
            response.body()
        }
    }

    override suspend fun castSecond(second: SecondDto) {
        client.post("$baseUrl/seconds") {
            contentType(ContentType.Application.Json)
            setBody(second)
        }
    }

    override suspend fun getComments(predictionId: String): List<CommentDto> {
        return client.get("$baseUrl/predictions/$predictionId/comments").body()
    }

    override suspend fun addComment(comment: CommentDto) {
        client.post("$baseUrl/comments") {
            contentType(ContentType.Application.Json)
            setBody(comment)
        }
    }

    override suspend fun getDiscourse(predictionId: String): List<DiscourseDto> {
        return client.get("$baseUrl/predictions/$predictionId/discourse").body()
    }

    override suspend fun postDiscourse(entry: DiscourseDto) {
        client.post("$baseUrl/discourse") {
            contentType(ContentType.Application.Json)
            setBody(entry)
        }
    }

    override suspend fun markDiscourseHelpful(entryId: String) {
        client.post("$baseUrl/discourse/$entryId/helpful")
    }

    override suspend fun flagDiscourse(entryId: String) {
        client.post("$baseUrl/discourse/$entryId/flag")
    }

    override suspend fun getSnapshots(predictionId: String, range: String): List<SecondingSnapshotDto> {
        return client.get("$baseUrl/predictions/$predictionId/snapshots") {
            parameter("range", range)
        } .body()
    }

    override suspend fun recordSnapshot(predictionId: String, snapshot: SecondingSnapshotDto) {
        client.post("$baseUrl/predictions/$predictionId/snapshots") {
            contentType(ContentType.Application.Json)
            setBody(snapshot)
        }
    }

    override suspend fun computeDownsampledRollup(predictionId: String, granularity: String) {
        client.post("$baseUrl/predictions/$predictionId/rollup") {
            parameter("granularity", granularity)
        }
    }

    override suspend fun submitPrediction(submission: Map<String, String>) {
        client.post("$baseUrl/submissions") {
            contentType(ContentType.Application.Json)
            setBody(submission)
        }
    }

    override suspend fun getSubmissions(): List<Map<String, String>> {
        return client.get("$baseUrl/submissions").body()
    }

    override suspend fun updateSubmissionState(submissionId: String, state: String) {
        client.put("$baseUrl/submissions/$submissionId/state") {
            parameter("state", state)
        }
    }

    override suspend fun submitAppeal(appeal: Map<String, String>) {
        client.post("$baseUrl/appeals") {
            contentType(ContentType.Application.Json)
            setBody(appeal)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): UserDto {
        return client.post("$baseUrl/auth/signin") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
        } .body()
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String, handle: String): UserDto {
        return client.post("$baseUrl/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password, "name" to name, "handle" to handle))
        } .body()
    }

    override suspend fun signInWithGoogle(idToken: String): UserDto {
        return client.post("$baseUrl/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("idToken" to idToken))
        } .body()
    }

    override suspend fun updateProfile(name: String, handle: String) {
        client.put("$baseUrl/profile") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to name, "handle" to handle))
        }
    }

    override suspend fun getKeepTabs(trackerUid: String): List<KeepTabDto> {
        return client.get("$baseUrl/keeptabs/$trackerUid").body()
    }

    override suspend fun trackUser(trackerUid: String, trackedUid: String) {
        client.post("$baseUrl/keeptabs") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("trackerUid" to trackerUid, "trackedUid" to trackedUid))
        }
    }

    override suspend fun untrackUser(trackerUid: String, trackedUid: String) {
        client.delete("$baseUrl/keeptabs") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("trackerUid" to trackerUid, "trackedUid" to trackedUid))
        }
    }

    override suspend fun syncKeepTabs() {
        client.post("$baseUrl/keeptabs/sync")
    }
}
