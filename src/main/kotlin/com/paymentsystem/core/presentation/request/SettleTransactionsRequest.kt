package com.paymentsystem.core.presentation.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class SettleTransactionsRequest(

    @field:NotNull(message = "Merchant ID is required")
    val merchantId: UUID,

    @field:Min(1, message = "Limit must be at least 1")
    val limit: Int = 100
)
