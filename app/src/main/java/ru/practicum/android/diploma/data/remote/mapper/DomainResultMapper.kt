package ru.practicum.android.diploma.data.remote.mapper

import ru.practicum.android.diploma.core.error.ApiError
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.DomainResult

object DomainResultMapper {

    fun <T> mapToDomainResult(apiResponse: ApiResponse<T>): DomainResult<T> {
        return when (apiResponse) {
            is ApiResponse.Success -> DomainResult.Success(apiResponse.data)
            is ApiResponse.Error -> DomainResult.Error(
                message = apiResponse.message,
                type = mapErrorType(apiResponse.code)
            )
            is ApiResponse.Loading -> throw IllegalStateException(
                "Loading state should not be mapped to DomainResult"
            )
        }
    }

    private fun mapErrorType(code: Int?): DomainResult.ErrorType {
        return when (code) {
            null -> DomainResult.ErrorType.NETWORK_ERROR
            ApiError.HTTP_FORBIDDEN -> DomainResult.ErrorType.ACCESS_DENIED
            ApiError.HTTP_NOT_FOUND -> DomainResult.ErrorType.NOT_FOUND
            in 500..599 -> DomainResult.ErrorType.SERVER_ERROR
            else -> DomainResult.ErrorType.UNKNOWN_ERROR
        }
    }
}
