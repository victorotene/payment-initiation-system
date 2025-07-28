package com.paymentsystem.core.domain.interfaces.repository

import com.paymentsystem.core.domain.Transaction
import com.paymentsystem.core.domain.entities.SettlementBatch
import java.util.UUID

interface SettlementRepository {
    suspend fun findSettlableTransactions(
        merchantId: UUID? = null,
        limit: Int = 100
    ): List<Transaction>

    suspend fun saveSettlementBatch(batch: SettlementBatch): SettlementBatch

    suspend fun updateTransactionsWithBatch(
        transactionIds: List<UUID>,
        batchId: UUID
    ): Int

    suspend fun findSettlementBatchById(batchId: UUID): SettlementBatch?

    suspend fun findSettlementBatchesByMerchant(
        merchantId: UUID,
        limit: Int = 50,
        offset: Int = 0
    ): List<SettlementBatch>
}
