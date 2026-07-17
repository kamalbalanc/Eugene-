package com.example.domain.usecase.auth

import com.example.domain.model.Session
import com.example.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Session.Authenticated> {
        return authRepository.signInWithEmail(email, password)
    }
}
