package ru.practicum.android.diploma.data.remote.mapper

import ru.practicum.android.diploma.core.error.ApiError
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.DomainResult

/**
 * Маппер для преобразования ApiResponse (Data-слой) в DomainResult (Domain-слой)
 */
object DomainResultMapper {

    private const val SERVER_ERROR_MIN_CODE = 500
    private const val SERVER_ERROR_MAX_CODE = 599

    fun <T> mapToDomainResult(apiResponse: ApiResponse<T>): DomainResult<T> {
        return when (apiResponse) {
            is ApiResponse.Success -> DomainResult.Success(apiResponse.data)
            is ApiResponse.Error -> DomainResult.Error(
                message = apiResponse.message,
                type = mapErrorType(apiResponse.code)
            )
            is ApiResponse.Loading -> error(
                "Loading state should not be mapped to DomainResult"
            )
        }
    }

    private fun mapErrorType(code: Int?): DomainResult.ErrorType {
        return when (code) {
            null -> DomainResult.ErrorType.NETWORK_ERROR
            ApiError.HTTP_FORBIDDEN -> DomainResult.ErrorType.ACCESS_DENIED
            ApiError.HTTP_NOT_FOUND -> DomainResult.ErrorType.NOT_FOUND
            in SERVER_ERROR_MIN_CODE..SERVER_ERROR_MAX_CODE -> DomainResult.ErrorType.SERVER_ERROR
            else -> DomainResult.ErrorType.UNKNOWN_ERROR
        }
    }
}
