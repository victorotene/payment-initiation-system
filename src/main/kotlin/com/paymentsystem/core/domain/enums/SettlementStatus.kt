package com.paymentsystem.core.domain.enums

enum class SettlementStatus {
    CREATED,
    PROCESSED,
    COMPLETED,
    FAILED;

    fun isActive(): Boolean = this == CREATED || this == PROCESSED

    fun isCompleted(): Boolean = this == COMPLETED

    fun isFailed(): Boolean = this == FAILED
}