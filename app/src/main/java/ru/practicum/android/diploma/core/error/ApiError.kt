package ru.practicum.android.diploma.core.error

import android.util.Log
import retrofit2.HttpException
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Sealed class для типов ошибок с сообщениями
 */
sealed class ApiError(val message: String, val code: Int? = null) {

    // HTTP ошибки
    data class AccessDenied(val httpCode: Int = HTTP_FORBIDDEN) :
        ApiError("Доступ запрещён. Проверьте токен авторизации", httpCode)

    data class NotFound(val httpCode: Int = HTTP_NOT_FOUND) :
        ApiError("Ресурс не найден", httpCode)

    data class InternalServerError(val httpCode: Int = HTTP_INTERNAL_ERROR) :
        ApiError("Внутренняя ошибка сервера", httpCode)

    data class HttpError(val httpCode: Int) :
        ApiError("HTTP ошибка: $httpCode", httpCode)

    // Сетевые ошибки
    data object NoInternet :
        ApiError("Нет подключения к интернету")

    data object Timeout :
        ApiError("Превышено время ожидания ответа")

    data class NetworkError(val details: String?) :
        ApiError("Ошибка сети: $details")

    // Ошибки базы данных
    data class DatabaseError(val details: String) :
        ApiError("Ошибка базы данных: $details")

    // Специфичные ошибки
    data class VacancyNotFound(val vacancyId: String) :
        ApiError("Вакансия не найдена", HTTP_NOT_FOUND)

    data class MappingError(val entityId: String?) :
        ApiError("Ошибка преобразования данных: $entityId")

    companion object {
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_INTERNAL_ERROR = 500
    }
}

/**
 * Enum для категорий логов
 */
enum class LogCategory(val tag: String) {
    AREA("AreaRepository"),
    INDUSTRY("IndustryRepository"),
    VACANCY("VacancyRepository"),
    DATABASE("DatabaseOperation"),
    NETWORK("NetworkOperation")
}

/**
 * Extension функция для обработки исключений
 */
fun Exception.toApiError(): ApiError {
    return when (this) {
        is HttpException -> {
            when (code()) {
                ApiError.HTTP_FORBIDDEN -> ApiError.AccessDenied()
                ApiError.HTTP_NOT_FOUND -> ApiError.NotFound()
                ApiError.HTTP_INTERNAL_ERROR -> ApiError.InternalServerError()
                else -> ApiError.HttpError(code())
            }
        }
        is SocketTimeoutException -> ApiError.Timeout
        is IOException -> ApiError.NetworkError(message)
        else -> ApiError.NetworkError(message)
    }
}

/**
 * Extension для преобразования ApiError в ApiResponse.Error
 */
fun ApiError.toApiResponse(): ApiResponse.Error {
    return ApiResponse.Error(message, code)
}

/**
 * Логирование ошибок с категорией
 */
fun ApiError.log(category: LogCategory, exception: Exception? = null) {
    when (this) {
        is ApiError.NoInternet,
        is ApiError.Timeout,
        is ApiError.AccessDenied,
        is ApiError.NotFound,
        is ApiError.InternalServerError,
        is ApiError.HttpError -> {
            Log.e(category.tag, message, exception)
        }
        is ApiError.NetworkError,
        is ApiError.DatabaseError,
        is ApiError.MappingError -> {
            Log.w(category.tag, message, exception)
        }
        is ApiError.VacancyNotFound -> {
            Log.e(category.tag, "Vacancy ${this.vacancyId}: $message", exception)
        }
    }
}

/**
 * Вспомогательная функция для безопасного выполнения сетевых запросов
 */
suspend fun <T> executeApiCall(
    category: LogCategory,
    onNoInternet: () -> Boolean = { false },
    block: suspend () -> T
): ApiResponse<T> {
    if (onNoInternet()) {
        val error = ApiError.NoInternet
        error.log(category)
        return error.toApiResponse()
    }

    return try {
        ApiResponse.Success(block())
    } catch (e: HttpException) {
        val error = e.toApiError()
        error.log(category, e)
        error.toApiResponse()
    } catch (e: SocketTimeoutException) {
        val error = ApiError.Timeout
        error.log(category, e)
        error.toApiResponse()
    } catch (e: IOException) {
        val error = ApiError.NetworkError(e.message)
        error.log(category, e)
        error.toApiResponse()
    }
}

/**
 * Вспомогательная функция для безопасного сохранения в БД
 */
suspend fun executeDatabaseOperation(
    category: LogCategory,
    operationName: String,
    block: suspend () -> Unit
) {
    runCatching {
        block()
    }.onFailure { exception ->
        when (exception) {
            is IOException -> {
                Log.w(category.tag, "Error during $operationName", exception)
            }
            is IllegalStateException -> {
                Log.w(category.tag, "Database state error during $operationName", exception)
            }
            else -> throw exception
        }
    }
}
