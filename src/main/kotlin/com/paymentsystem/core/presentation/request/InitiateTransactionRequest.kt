package com.paymentsystem.core.presentation.request

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.UUID

data class InitiateTransactionRequest(

    @field:NotNull(message = "Merchant ID is required")
    val merchantId: UUID,

    @field:NotBlank(message = "Merchant reference is required")
    @field:Size(min = 1, max = 255, message = "Merchant reference must be between 1 and 255 characters")
    val merchantRef: String,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    @field:Digits(integer = 16, fraction = 2, message = "Amount must have at most 2 decimal places")
    val amount: BigDecimal,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val currency: String,

    @field:NotBlank(message = "Idempotency key is required")
    @field:Size(min = 1, max = 255, message = "Idempotency key must be between 1 and 255 characters")
    val idempotencyKey: String
)
