package com.example.domain

import com.example.domain.model.*
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PredictionTest {

    private val now = Instant.fromEpochMilliseconds(1000000)
    private val closesAt = Instant.fromEpochMilliseconds(2000000)
    private val resolvesAt = Instant.fromEpochMilliseconds(3000000)

    private fun createOption(id: String, imageUrl: String? = null) = PredictionOption(
        id = id,
        text = "Option $id",
        seconds = 0,
        percentage = 0,
        accent = PredictionAccent.BLUE,
        imageUrl = imageUrl
    )

    @Test
    fun testValidPredictionWithTwoOutcomes() {
        val prediction = Prediction(
            id = "1",
            category = PredictionCategory.TECHNOLOGY,
            status = PredictionStatus.LIVE,
            publishStatus = PredictionPublishStatus.APPROVED,
            title = "Valid Title",
            description = "Desc",
            rulesDescription = "Rules",
            createdAt = now,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            totalSeconds = 0,
            options = listOf(createOption("a"), createOption("b")),
            resolutionSource = "Source",
            createdBy = "user1"
        )
        assertNotNull(prediction)
    }

    @Test
    fun testValidPredictionWithSixOutcomes() {
        val prediction = Prediction(
            id = "1",
            category = PredictionCategory.TECHNOLOGY,
            status = PredictionStatus.LIVE,
            publishStatus = PredictionPublishStatus.APPROVED,
            title = "Valid Title",
            description = "Desc",
            rulesDescription = "Rules",
            createdAt = now,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            totalSeconds = 0,
            options = (1..6).map { createOption(it.toString()) },
            resolutionSource = "Source",
            createdBy = "user1"
        )
        assertNotNull(prediction)
    }

    @Test
    fun testPredictionRejectsOneOutcome() {
        assertFailsWith<IllegalArgumentException> {
            Prediction(
                id = "1",
                category = PredictionCategory.TECHNOLOGY,
                status = PredictionStatus.LIVE,
                publishStatus = PredictionPublishStatus.APPROVED,
                title = "Title",
                description = "Desc",
                rulesDescription = "Rules",
                createdAt = now,
                closesAt = closesAt,
                resolvesAt = resolvesAt,
                totalSeconds = 0,
                options = listOf(createOption("a")),
                resolutionSource = "Source",
                createdBy = "user1"
            )
        }
    }

    @Test
    fun testPredictionRejectsSevenOutcomes() {
        assertFailsWith<IllegalArgumentException> {
            Prediction(
                id = "1",
                category = PredictionCategory.TECHNOLOGY,
                status = PredictionStatus.LIVE,
                publishStatus = PredictionPublishStatus.APPROVED,
                title = "Title",
                description = "Desc",
                rulesDescription = "Rules",
                createdAt = now,
                closesAt = closesAt,
                resolvesAt = resolvesAt,
                totalSeconds = 0,
                options = (1..7).map { createOption(it.toString()) },
                resolutionSource = "Source",
                createdBy = "user1"
            )
        }
    }

    @Test
    fun testPredictionRejectsInvalidDateOrder() {
        assertFailsWith<IllegalArgumentException> {
            Prediction(
                id = "1",
                category = PredictionCategory.TECHNOLOGY,
                status = PredictionStatus.LIVE,
                publishStatus = PredictionPublishStatus.APPROVED,
                title = "Title",
                description = "Desc",
                rulesDescription = "Rules",
                createdAt = now,
                closesAt = resolvesAt, // closes at same time or after resolvesAt
                resolvesAt = closesAt,
                totalSeconds = 0,
                options = listOf(createOption("a"), createOption("b")),
                resolutionSource = "Source",
                createdBy = "user1"
            )
        }
    }

    @Test
    fun testPredictionRejectsPartialImages() {
        assertFailsWith<IllegalArgumentException> {
            Prediction(
                id = "1",
                category = PredictionCategory.TECHNOLOGY,
                status = PredictionStatus.LIVE,
                publishStatus = PredictionPublishStatus.APPROVED,
                title = "Title",
                description = "Desc",
                rulesDescription = "Rules",
                createdAt = now,
                closesAt = closesAt,
                resolvesAt = resolvesAt,
                totalSeconds = 0,
                options = listOf(
                    createOption("a", "http://image.png"),
                    createOption("b", null) // only one has imageUrl
                ),
                resolutionSource = "Source",
                createdBy = "user1"
            )
        }
    }

    @Test
    fun testPredictionAcceptsAllImages() {
        val prediction = Prediction(
            id = "1",
            category = PredictionCategory.TECHNOLOGY,
            status = PredictionStatus.LIVE,
            publishStatus = PredictionPublishStatus.APPROVED,
            title = "Title",
            description = "Desc",
            rulesDescription = "Rules",
            createdAt = now,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            totalSeconds = 0,
            options = listOf(
                createOption("a", "http://image1.png"),
                createOption("b", "http://image2.png")
            ),
            resolutionSource = "Source",
            createdBy = "user1"
        )
        assertNotNull(prediction)
    }
}
