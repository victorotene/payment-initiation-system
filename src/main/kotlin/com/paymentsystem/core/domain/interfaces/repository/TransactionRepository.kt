package com.paymentsystem.core.domain.interfaces.repository

import com.paymentsystem.core.application.common.PageRequest
import com.paymentsystem.core.application.common.PageResult
import com.paymentsystem.core.domain.Transaction
import com.paymentsystem.core.domain.enums.TransactionStatus
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

interface TransactionRepository {
    suspend fun findByIdempotencyKey(idempotencyKey: String): Transaction?
    suspend fun save(transaction: Transaction): Transaction
    suspend fun findById(transactionId: UUID): Transaction?
    suspend fun update(transaction: Transaction): Transaction
    suspend fun findTransactionsByMerchantAndDateRange(
        merchantId: UUID,
        startDate: ZonedDateTime,
        endDate: ZonedDateTime,
        status: TransactionStatus?,
        limit: Int,
        offset: Int
    ): List<Transaction>

    suspend fun findByMerchantIdWithFilters(
        merchantId: UUID,
        status: TransactionStatus? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        pageRequest: PageRequest
    ): PageResult<Transaction>
}

/* FOR MORE FUNCTIONALITIES
        suspend fun findTransactionsForRetry(
        statuses: List<TransactionStatus>,
        maxRetries: Int,
        olderThan: ZonedDateTime,
        limit: Int
    ): List<Transaction>

    suspend fun findProcessingTransactionsForRetry(limit: Int): List<Transaction>

 */