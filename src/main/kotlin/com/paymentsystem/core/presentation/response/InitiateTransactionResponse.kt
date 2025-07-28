package com.paymentsystem.core.presentation.response

import com.paymentsystem.core.domain.enums.DebitStatus
import com.paymentsystem.core.domain.enums.TransactionStatus
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class InitiateTransactionResponse(
    val id: UUID,
    val merchantId: UUID,
    val merchantRef: String,
    val internalRef: String,
    val amount: BigDecimal,
    val currency: String,
    val fee: BigDecimal,
    val netAmount: BigDecimal,
    val status: String,
    val customerSimulatedDebitStatus: String,
    val idempotencyKey: String,
    val createdAt: ZonedDateTime,
    val message: String? = null
)