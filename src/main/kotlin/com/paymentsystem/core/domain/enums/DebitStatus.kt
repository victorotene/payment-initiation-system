package com.paymentsystem.core.domain.enums

enum class DebitStatus(val value: String) {
    PENDING("PENDING"),
    DEBITED("DEBITED"),
    FAILED("FAILED");

    companion object {
        fun fromValue(value: String): DebitStatus = values().find { it.value == value }
            ?: throw IllegalArgumentException("Invalid DebitStatus value: $value")
    }
}