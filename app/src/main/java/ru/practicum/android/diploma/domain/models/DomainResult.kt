package ru.practicum.android.diploma.domain.models

sealed class DomainResult<out T> {

    data class Success<T>(val data: T) : DomainResult<T>()

    data class Error(
        val message: String,
        val type: ErrorType
    ) : DomainResult<Nothing>()

    enum class ErrorType {
        NETWORK_ERROR,      // Нет интернета, таймаут
        SERVER_ERROR,       // Ошибка сервера (HTTP 500)
        NOT_FOUND,          // Ресурс не найден (HTTP 404)
        ACCESS_DENIED,      // Доступ запрещён (HTTP 403)
        DATABASE_ERROR,     // Ошибка БД
        UNKNOWN_ERROR       // Неизвестная ошибка
    }
}

// Дополнительные утилитные функции
fun <T> DomainResult<T>.getOrNull(): T? = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> null
}

fun <T> DomainResult<T>.getOrThrow(): T = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> throw RuntimeException(message)
}
