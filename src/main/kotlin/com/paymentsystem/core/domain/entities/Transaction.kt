package com.paymentsystem.core.domain

import com.paymentsystem.core.domain.events.DomainEvent
import com.paymentsystem.core.domain.events.TransactionCompleted
import com.paymentsystem.core.domain.events.TransactionInitiated
import com.paymentsystem.core.domain.events.TransactionSettled
import com.paymentsystem.core.domain.valueobjects.Money
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.enums.DebitStatus
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

data class Transaction(
    val id: UUID,
    val merchantId: UUID,
    val merchantRef: String,
    val internalRef: String,
    val amount: Money,
    val fee: Money,
    val netAmount: Money,
    val retryCount: Int,
    val status: TransactionStatus,
    val idempotencyKey: String,
    val customerSimulatedDebitStatus: DebitStatus,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val settlementBatchId: UUID? = null,
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()
) {
    companion object {
        fun initiate(
            merchantId: UUID,
            merchantRef: String,
            amount: Money,
            fee: Money,
            idempotencyKey: String
        ): Transaction {
            val transactionId = UUID.randomUUID()
            val internalRef = generateInternalRef()
            val netAmount = amount.subtract(fee)
            val now = ZonedDateTime.now()

            val transaction = Transaction(
                id = transactionId,
                merchantId = merchantId,
                merchantRef = merchantRef,
                internalRef = internalRef,
                amount = amount,
                fee = fee,
                netAmount = netAmount,
                retryCount = 0,
                status = TransactionStatus.INITIATED,
                idempotencyKey = idempotencyKey,
                customerSimulatedDebitStatus = DebitStatus.PENDING,
                createdAt = now,
                updatedAt = now
            )

            transaction.addDomainEvent(
                TransactionInitiated(
                    transactionId = transactionId,
                    merchantId = merchantId,
                    merchantRef = merchantRef,
                    internalRef = internalRef,
                    amount = amount,
                    idempotencyKey = idempotencyKey
                )
            )

            return transaction
        }

        fun createFailed(
            idempotencyKey: String,
            merchantId: UUID,
            amount: Money,
            fee: Money,
            //reason: String
        ): Transaction {
            val transactionId = UUID.randomUUID()
            val internalRef = generateInternalRef()
            val netAmount = amount.subtract(fee)
            val now = ZonedDateTime.now()

            val transaction = Transaction(
                id = transactionId,
                merchantId = merchantId,
                merchantRef = "",
                internalRef = internalRef,
                amount = amount,
                fee = fee,
                netAmount = netAmount,
                retryCount = 0,
                status = TransactionStatus.FAILED,
                idempotencyKey = idempotencyKey,
                customerSimulatedDebitStatus = DebitStatus.FAILED,
                createdAt = now,
                updatedAt = now
            )

            transaction.addDomainEvent(
                TransactionCompleted(
                    transactionId = transactionId,
                    merchantId = merchantId,
                    internalRef = internalRef,
                    status = TransactionStatus.FAILED,
                    amount = amount,
                    fee = fee,
                    netAmount = netAmount,
                    //failureReason = reason
                )
            )

            return transaction
        }

        private fun generateInternalRef(): String {
            val timestamp = System.currentTimeMillis()
            val random = Random.nextInt(1000, 9999)
            return "TXN_${timestamp}_$random"
        }
    }

    fun getTotalMerchantDebit(): Money {
        return amount.add(fee)
    }

    fun complete(success: Boolean): Transaction {
        require(status == TransactionStatus.INITIATED || status == TransactionStatus.PENDING) {
            "Only initiated or pending transactions can be completed"
        }

        val newStatus = if (success) TransactionStatus.SUCCESS else TransactionStatus.FAILED
        val debitStatus = if (success) DebitStatus.DEBITED else DebitStatus.FAILED

        val updatedTransaction = copy(
            status = newStatus,
            customerSimulatedDebitStatus = debitStatus,
            updatedAt = ZonedDateTime.now()
        )

        updatedTransaction.addDomainEvent(
            TransactionCompleted(
                transactionId = id,
                merchantId = merchantId,
                internalRef = internalRef,
                status = newStatus,
                amount = amount,
                fee = fee,
                netAmount = netAmount
            )
        )

        return updatedTransaction
    }

    fun markAsPending(): Transaction {
        require(status == TransactionStatus.INITIATED) { "Only initiated transactions can be marked as pending" }

        return copy(
            status = TransactionStatus.PENDING,
            updatedAt = ZonedDateTime.now()
        )
    }

    fun settle(batchId: UUID): Transaction {
        require(status == TransactionStatus.SUCCESS) { "Only successful transactions can be settled" }
        require(settlementBatchId == null) { "Transaction is already settled" }

        val settledTransaction = copy(
            status = TransactionStatus.SETTLED,
            settlementBatchId = batchId,
            updatedAt = ZonedDateTime.now()
        )

        settledTransaction.addDomainEvent(
            TransactionSettled(
                transactionId = id,
                merchantId = merchantId,
                batchId = batchId,
                batchRef = "",
                amount = amount,
                fee = fee,
                netAmount = netAmount
            )
        )

        return settledTransaction
    }

    fun incrementRetryCount(): Transaction {
        return copy(
            retryCount = retryCount + 1,
            updatedAt = ZonedDateTime.now()
        )
    }

    fun canBeRetried(): Boolean {
        return status == TransactionStatus.PENDING || status == TransactionStatus.INITIATED
    }

    fun canBeSettled(): Boolean {
        return status == TransactionStatus.SUCCESS && settlementBatchId == null
    }

    fun isPending(): Boolean {
        return status == TransactionStatus.PENDING
    }

    fun isCompleted(): Boolean {
        return status == TransactionStatus.SUCCESS || status == TransactionStatus.FAILED
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    fun clearDomainEvents(): Transaction {
        domainEvents.clear()
        return this
    }

    private fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}