package com.paymentsystem.core.domain.entities

import com.paymentsystem.core.domain.events.DomainEvent
import com.paymentsystem.core.domain.events.SettlementBatchCreated
import com.paymentsystem.core.domain.valueobjects.Money
import com.paymentsystem.core.domain.enums.SettlementStatus
import java.time.ZonedDateTime
import java.util.UUID

data class SettlementBatch(
    val id: UUID,
    val batchRef: String,
    val merchantId: UUID,
    val totalAmount: Money,
    val totalFee: Money,
    val netAmount: Money,
    val transactionCount: Int,
    val status: SettlementStatus,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()
) {
    companion object {
        fun create(
            merchantId: UUID,
            totalAmount: Money,
            totalFee: Money,
            transactionCount: Int
        ): SettlementBatch {
            require(transactionCount > 0) { "Cannot create settlement batch with no transactions" }
            require(totalAmount.currency == totalFee.currency) {
                "Total amount and fee must use the same currency"
            }

            val netAmount = totalAmount.subtract(totalFee)
            val batchId = UUID.randomUUID()
            val batchRef = generateBatchRef()
            val now = ZonedDateTime.now()

            val batch = SettlementBatch(
                id = batchId,
                batchRef = batchRef,
                merchantId = merchantId,
                totalAmount = totalAmount,
                totalFee = totalFee,
                netAmount = netAmount,
                transactionCount = transactionCount,
                status = SettlementStatus.CREATED,
                createdAt = now,
                updatedAt = now
            )

            batch.addDomainEvent(
                SettlementBatchCreated(
                    batchId = batchId,
                    batchRef = batchRef,
                    merchantId = merchantId,
                    totalAmount = totalAmount,
                    totalFee = totalFee,
                    netAmount = netAmount,
                    transactionCount = transactionCount
                )
            )

            return batch
        }

        private fun generateBatchRef(): String {
            val timestamp = System.currentTimeMillis()
            val random = (10000..99999).random()
            return "BATCH_${timestamp}_$random"
        }
    }

    fun process(): SettlementBatch {
        require(status == SettlementStatus.CREATED) { "Only created batches can be processed" }
        return copy(status = SettlementStatus.PROCESSED, updatedAt = ZonedDateTime.now())
    }

    fun complete(): SettlementBatch {
        require(status == SettlementStatus.PROCESSED) { "Only processed batches can be completed" }
        return copy(
            status = SettlementStatus.COMPLETED,
            updatedAt = ZonedDateTime.now()
        )
    }

    fun fail(reason: String): SettlementBatch {
        require(status == SettlementStatus.CREATED || status == SettlementStatus.PROCESSED) {
            "Only created or processed batches can be failed"
        }
        return copy(
            status = SettlementStatus.FAILED,
            updatedAt = ZonedDateTime.now()
        )
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    fun clearDomainEvents(): SettlementBatch {
        domainEvents.clear()
        return this
    }

    private fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}