package com.paymentsystem.core.domain.enums

enum class MerchantStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    fun isActive(): Boolean = this == ACTIVE
}