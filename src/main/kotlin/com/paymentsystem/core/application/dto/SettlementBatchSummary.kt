package com.paymentsystem.core.application.dto

import java.math.BigDecimal
import java.util.UUID

data class SettlementBatchSummary(
    val batchId: UUID?,
    val batchRef: String,
    val merchantId: UUID,
    val totalAmount: BigDecimal,
    val totalFee: BigDecimal,
    val netAmount: BigDecimal,
    val currency: String,
    val transactionCount: Int,
    val message: String
)