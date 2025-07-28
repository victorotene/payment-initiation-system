package com.paymentsystem.core.presentation.response

import java.time.ZonedDateTime

data class ErrorResponse(
    val message: String,
    val code: String,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
)