package com.paymentsystem.core.presentation.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateMerchantRequest(
    @field:NotBlank(message = "Business name is required")
    @field:Size(min = 2, max = 255, message = "Business name must be between 2 and 255 characters")
    val businessName: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,


    @field:NotBlank(message = "Settlement account is required")
    @field:Pattern(
        regexp = "^\\d{10}$",
        message = "Settlement account must be exactly 10 digits with no special characters"
    )
    val settlementAccount: String,

    @field:NotNull(message = "Balance is required")
    @field:DecimalMin(value = "10.00", message = "Balance must be at least 10.00")
    @field:Digits(integer = 10, fraction = 2, message = "Balance must have at most 2 decimal places")
    val balance: BigDecimal
)
