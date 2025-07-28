package com.paymentsystem.core.domain.valueobjects

enum class MerchantStatus {
    ACTIVE,
    SUSPENDED,
    BLOCKED;

    fun isActive(): Boolean = this == ACTIVE
}