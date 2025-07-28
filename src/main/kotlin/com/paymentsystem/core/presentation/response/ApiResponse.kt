package com.paymentsystem.core.presentation.response

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T?, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(true, message, data)
        }

        fun <T> failure(message: String = "An error occurred"): ApiResponse<T> {
            return ApiResponse(false, message, null)
        }
    }
}