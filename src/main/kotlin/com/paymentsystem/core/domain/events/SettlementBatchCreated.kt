package com.paymentsystem.core.domain.events

import com.paymentsystem.core.domain.valueobjects.Money
import java.util.UUID

data class SettlementBatchCreated(
    val batchId: UUID,
    val batchRef: String,
    val merchantId: UUID,
    val totalAmount: Money,
    val totalFee: Money,
    val netAmount: Money,
    val transactionCount: Int,
    //val transactionIds: List<UUID>
) : DomainEvent()