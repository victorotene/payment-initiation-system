package com.paymentsystem.core.application.dto

import com.paymentsystem.core.domain.Transaction
import com.paymentsystem.core.domain.enums.DebitStatus
import com.paymentsystem.core.domain.enums.TransactionStatus
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class TransactionResult(
    val id: UUID,
    val merchantId: UUID,
    val merchantRef: String,
    val internalRef: String,
    val amount: BigDecimal,
    val currency: String,
    val fee: BigDecimal,
    val netAmount: BigDecimal,
    val status: TransactionStatus,
    val customerSimulatedDebitStatus: DebitStatus,
    val retryCount: Int,
    val settlementBatchId: UUID?,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val idempotencyKey: String,
) {
    companion object {
        fun fromTransaction(transaction: Transaction, message: String): TransactionResult {
            return TransactionResult(
                id = transaction.id,
                merchantId = transaction.merchantId,
                merchantRef = transaction.merchantRef,
                internalRef = transaction.internalRef,
                amount = transaction.amount.amount,
                currency = transaction.amount.currency.toString(),
                fee = transaction.fee.amount,
                netAmount = transaction.netAmount.amount,
                status = transaction.status,
                customerSimulatedDebitStatus = transaction.customerSimulatedDebitStatus,
                retryCount = transaction.retryCount,
                settlementBatchId = transaction.settlementBatchId,
                createdAt = transaction.createdAt,
                updatedAt = transaction.updatedAt,
                idempotencyKey = transaction.idempotencyKey
            )
        }
    }
}