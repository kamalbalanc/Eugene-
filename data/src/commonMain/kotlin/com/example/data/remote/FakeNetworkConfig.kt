package com.example.data.remote

enum class FakeScenario {
    SUCCESS,
    SLOW_SUCCESS,
    EMPTY_RESULT,
    NETWORK_ERROR,
    SERVER_ERROR,
    VALIDATION_ERROR,
    TRANSIENT_ERROR,
    PERSISTENT_ERROR,
    CONFLICT_STALE_CLOSE,
    RATE_LIMITED,
    NO_ROLLUP_YET
}

class FakeNetworkConfig(
    var latencyMs: Long = 200L,
    var failureRate: Float = 0.0f,
    var forcedScenario: FakeScenario? = null,
    var isOffline: Boolean = false,
    var isSessionExpired: Boolean = false
) {
    // For tracking TRANSIENT_ERROR scenario locally
    var transientAttempts: Int = 0
}
