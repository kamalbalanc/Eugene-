package com.example.domain.repository

import com.example.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<Session>
    suspend fun signInWithEmail(email: String, password: String): Result<Session.Authenticated>
    suspend fun signUpWithEmail(email: String, password: String, name: String, handle: String): Result<Session.Authenticated>
    suspend fun signInWithGoogle(idToken: String): Result<Session.Authenticated>
    suspend fun signOut(): Result<Unit>
    suspend fun updateProfile(name: String, handle: String): Result<Unit>
}
