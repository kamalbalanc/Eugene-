package com.example.data.remote

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class FakeEugeneApiServiceTest {

    private lateinit var config: FakeNetworkConfig
    private lateinit var service: FakeEugeneApiService

    @BeforeTest
    fun setUp() {
        config = FakeNetworkConfig(latencyMs = 0L) // Set 0 latency for fast test runs
        service = FakeEugeneApiService(config)
    }

    @Test
    fun testDefaultSeedLoaded() = runTest {
        val predictions = service.getPredictions()
        assertFalse(predictions.isEmpty(), "Default seed data should load multiple predictions")
        assertEquals(6, predictions.size, "Should contain the default 6 seeded predictions")
    }

    @Test
    fun testWipeAllData() = runTest {
        service.wipeAllData()
        val predictions = service.getPredictions()
        assertTrue(predictions.isEmpty(), "Predictions should be completely empty after wipeAllData")
    }

    @Test
    fun testResetToSeedData() = runTest {
        service.wipeAllData()
        service.resetToSeedData()
        val predictions = service.getPredictions()
        assertEquals(6, predictions.size, "Predictions should restore to 6 after resetToSeedData")
    }

    @Test
    fun testOfflineSimulation() = runTest {
        config.isOffline = true
        val exception = assertFailsWith<Exception> {
            service.getPredictions()
        }
        assertTrue(exception.message!!.contains("Offline Simulation"), "Should throw offline simulation exception")
    }

    @Test
    fun testEmptyResultScenario() = runTest {
        config.forcedScenario = FakeScenario.EMPTY_RESULT
        val predictions = service.getPredictions()
        assertTrue(predictions.isEmpty(), "Should return empty list for EMPTY_RESULT scenario")
    }

    @Test
    fun testNetworkErrorScenario() = runTest {
        config.forcedScenario = FakeScenario.NETWORK_ERROR
        val exception = assertFailsWith<Exception> {
            service.getPredictions()
        }
        assertTrue(exception.message!!.contains("Simulated network failure"), "Should throw network error exception")
    }

    @Test
    fun testRateLimitedScenario() = runTest {
        config.forcedScenario = FakeScenario.RATE_LIMITED
        val exception = assertFailsWith<Exception> {
            service.getPredictions()
        }
        assertTrue(exception.message!!.contains("Rate limited"), "Should throw rate limit exception")
    }

    @Test
    fun testConflictStaleCloseScenario() = runTest {
        config.forcedScenario = FakeScenario.CONFLICT_STALE_CLOSE
        val second = com.example.data.remote.dto.SecondDto(
            id = "s_1",
            predictionId = "pred_2_outcomes",
            optionId = "opt_2_yes",
            reasoning = "Because",
            castAt = kotlinx.datetime.Clock.System.now(),
            status = "PENDING"
        )
        val exception = assertFailsWith<Exception> {
            service.castSecond(second)
        }
        assertTrue(exception.message!!.contains("CONFLICT_STALE_CLOSE"), "Should throw conflict stale close exception")
    }

    @Test
    fun testTransientErrorScenario() = runTest {
        config.forcedScenario = FakeScenario.TRANSIENT_ERROR
        
        // 1st attempt should fail
        val exception1 = assertFailsWith<Exception> {
            service.getPredictions()
        }
        assertTrue(exception1.message!!.contains("Transient error"), "1st attempt should fail with transient error")

        // 2nd attempt should fail
        val exception2 = assertFailsWith<Exception> {
            service.getPredictions()
        }
        assertTrue(exception2.message!!.contains("Transient error"), "2nd attempt should fail with transient error")

        // 3rd attempt should succeed
        val predictions = service.getPredictions()
        assertEquals(6, predictions.size, "3rd attempt should succeed and return predictions list")
    }

    @Test
    fun testSeedEdgeCases() = runTest {
        service.seedEdgeCases()
        val predictions = service.getPredictions()
        // Default 6 + 3 edge cases = 9 predictions
        assertEquals(9, predictions.size, "Should contain default predictions plus edge cases")
        
        val contentRemoved = predictions.find { it.id == "pred_content_removed" }
        assertNotNull(contentRemoved, "Should find content removed edge case prediction")
        assertEquals(500, contentRemoved.flagCount, "Content removed prediction should have 500 flags")
    }
}
