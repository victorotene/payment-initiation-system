package com.paymentsystem.core.domain.events

import com.paymentsystem.core.domain.valueobjects.Money
import java.util.UUID

data class TransactionSettled(
    val transactionId: UUID,
    val merchantId: UUID,
    val batchId: UUID,
    val batchRef: String,
    val amount: Money,
    val fee: Money,
    val netAmount: Money
) : DomainEvent()