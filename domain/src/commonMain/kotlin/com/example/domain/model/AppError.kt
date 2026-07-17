package com.example.domain.model

sealed interface AppError {
    data object NoConnection : AppError
    data class Validation(val field: String, val reason: String) : AppError
    data class NotAuthenticated(val action: String) : AppError
    data class Server(val code: Int?, val message: String) : AppError
    data class Unknown(val cause: Throwable) : AppError
}

class AppErrorException(val error: AppError) : Exception(error.toString())
