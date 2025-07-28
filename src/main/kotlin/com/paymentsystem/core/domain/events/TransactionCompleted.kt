package com.paymentsystem.core.domain.events

import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.valueobjects.Money
import java.util.UUID

data class TransactionCompleted(
    val transactionId: UUID,
    val merchantId: UUID,
    val internalRef: String,
    val status: TransactionStatus,
    val amount: Money,
    val fee: Money,
    val netAmount: Money
) : DomainEvent()