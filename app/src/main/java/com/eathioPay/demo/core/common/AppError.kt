package com.eathioPay.demo.core.common

sealed interface AppError {
    data class Validation(val message: String) : AppError
    data class Network(val message: String) : AppError
    data class Unknown(val message: String) : AppError
}

fun Throwable.toAppError(): AppError = when (this) {
    is IllegalArgumentException -> AppError.Validation(message ?: "Invalid input")
    else -> AppError.Network(message ?: "Something went wrong")
}

fun AppError.message(): String = when (this) {
    is AppError.Network -> message
    is AppError.Unknown -> message
    is AppError.Validation -> message
}
