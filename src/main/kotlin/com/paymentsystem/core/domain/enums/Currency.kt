package com.paymentsystem.core.domain.enums

enum class Currency(val code: String, val symbol: String) {
    NGN("NGN", "₦"),
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£");

    companion object {
        fun fromCode(code: String): Currency {
            return values().find { it.code == code }
                ?: throw IllegalArgumentException("Invalid currency code: $code")
        }
    }
}