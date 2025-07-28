package com.paymentsystem.core.domain.enums

enum class TransactionStatus {
    INITIATED,
    PENDING,
    SUCCESS,
    FAILED,
    SETTLED;

    fun canTransitionTo(newStatus: TransactionStatus): Boolean {
        return when (this) {
            INITIATED -> newStatus == PENDING
            PENDING -> newStatus in listOf(SUCCESS, FAILED)
            SUCCESS -> newStatus == SETTLED
            FAILED, SETTLED -> false
        }
    }
}
