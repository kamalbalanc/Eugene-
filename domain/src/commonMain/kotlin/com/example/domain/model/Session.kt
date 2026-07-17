package com.example.domain.model

sealed interface Session {
    data object Guest : Session
    data class Authenticated(
        val uid: String,
        val email: String,
        val name: String,
        val handle: String,
        val avatarUrl: String,
        val accuracy: Int,
        val reputation: Int,
        val resolvedPredictionCount: Int = 0
    ) : Session
}

val Session.isAuthenticated: Boolean get() = this is Session.Authenticated
val Session.Authenticated.isAccuracyEligible: Boolean get() = resolvedPredictionCount >= 5
