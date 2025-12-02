package ru.practicum.android.diploma.domain.models

/**
 * Sealed-класс для представления результата операции в Domain-слое
 */
sealed class DomainResult<out T> {

    data class Success<T>(val data: T) : DomainResult<T>()

    data class Error(
        val message: String,
        val type: ErrorType
    ) : DomainResult<Nothing>()

    enum class ErrorType {
        NETWORK_ERROR,
        SERVER_ERROR,
        NOT_FOUND,
        ACCESS_DENIED,
        DATABASE_ERROR,
        UNKNOWN_ERROR
    }
}

/**
 * Extension-функция для преобразования DomainResult в Success или null
 */
fun <T> DomainResult<T>.getOrNull(): T? = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> null
}

/**
 * Extension-функция для получения данных или выбрасывания DomainException
 */
fun <T> DomainResult<T>.getOrThrow(): T = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Error -> throw DomainException(message, type)
}

/**
 * Специфичное исключение для Domain-слоя
 */
class DomainException(
    override val message: String,
    val errorType: DomainResult.ErrorType
) : RuntimeException(message)

/**
 * Extension-функция для выполнения действия только при Success
 */
inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) {
        action(data)
    }
    return this
}

/**
 * Extension-функция для выполнения действия только при Error
 */
inline fun <T> DomainResult<T>.onError(action: (String, DomainResult.ErrorType) -> Unit): DomainResult<T> {
    if (this is DomainResult.Error) {
        action(message, type)
    }
    return this
}
