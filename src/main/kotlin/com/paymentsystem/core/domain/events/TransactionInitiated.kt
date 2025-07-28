package com.paymentsystem.core.domain.events

import com.paymentsystem.core.domain.valueobjects.Money
import java.util.UUID

data class TransactionInitiated(
    val transactionId: UUID,
    val merchantId: UUID,
    val merchantRef: String,
    val internalRef: String,
    val amount: Money,
    val idempotencyKey: String
) : DomainEvent()