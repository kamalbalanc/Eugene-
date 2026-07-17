package com.example.domain.usecase.auth

import com.example.domain.model.Session
import com.example.domain.repository.AuthRepository

class SignUpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        handle: String
    ): Result<Session.Authenticated> {
        return authRepository.signUpWithEmail(email, password, name, handle)
    }
}
