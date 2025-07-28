package com.paymentsystem.core.application.commandhandlers

import com.paymentsystem.core.application.commands.SettleTransactionsCommand
import com.paymentsystem.core.application.dto.SettlementBatchSummary
import com.paymentsystem.core.application.interfaces.CommandHandler
import com.paymentsystem.core.domain.entities.SettlementBatch
import com.paymentsystem.core.domain.exceptions.MerchantNotFoundException
import com.paymentsystem.core.domain.interfaces.repository.MerchantRepository
import com.paymentsystem.core.domain.interfaces.repository.SettlementRepository
import com.paymentsystem.core.domain.valueobjects.Money
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@Component
@Transactional
class SettleTransactionsHandler(
    private val settlementRepository: SettlementRepository,
    private val merchantRepository: MerchantRepository
) : CommandHandler<SettleTransactionsCommand, SettlementBatchSummary> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun handle(command: SettleTransactionsCommand): SettlementBatchSummary {
        logger.info(
            "Starting settlement process for merchant: {}, limit: {}",
            command.merchantId, command.limit
        )

        val merchant = merchantRepository.findById(command.merchantId)
            ?: throw MerchantNotFoundException("Merchant ${command.merchantId} not found")

        val settlableTransactions = settlementRepository.findSettlableTransactions(
            merchantId = command.merchantId,
            limit = command.limit
        )

        if (settlableTransactions.isEmpty()) {
            logger.info("No settlable transactions found for merchant: {}", command.merchantId)
            return SettlementBatchSummary(
                batchId = null,
                batchRef = "",
                merchantId = command.merchantId,
                totalAmount = BigDecimal.ZERO,
                totalFee = BigDecimal.ZERO,
                netAmount = BigDecimal.ZERO,
                currency = "USD", // Default currency
                transactionCount = 0,
                message = "No transactions available for settlement"
            )
        }

        val transactionsByCurrency = settlableTransactions.groupBy { it.amount.currency }

        if (transactionsByCurrency.size > 1) {
            logger.warn(
                "Multiple currencies found in settlable transactions for merchant: {}",
                command.merchantId
            )
        }

        val primaryCurrency = transactionsByCurrency.keys.first()
        val transactionsToSettle = transactionsByCurrency[primaryCurrency]!!

        val totalAmount = transactionsToSettle
            .map { it.amount.amount }
            .fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }

        val totalFee = transactionsToSettle
            .map { it.fee.amount }
            .fold(BigDecimal.ZERO) { acc, fee -> acc.add(fee) }

        logger.info(
            "Settlement calculation - Total amount: {}, Total fee: {}, Transaction count: {}",
            totalAmount, totalFee, transactionsToSettle.size
        )

        val settlementBatch = SettlementBatch.create(
            merchantId = command.merchantId,
            totalAmount = Money(totalAmount, primaryCurrency),
            totalFee = Money(totalFee, primaryCurrency),
            transactionCount = transactionsToSettle.size
        )

        val savedBatch = settlementRepository.saveSettlementBatch(settlementBatch)

        val transactionIds = transactionsToSettle.map { it.id }
        val updatedCount = settlementRepository.updateTransactionsWithBatch(
            transactionIds, savedBatch.id
        )

        if (updatedCount != transactionsToSettle.size) {
            logger.warn(
                "Expected to update {} transactions but updated {} for batch {}",
                transactionsToSettle.size, updatedCount, savedBatch.id
            )
        }

        logger.info(
            "Settlement batch created successfully: {} with {} transactions",
            savedBatch.id, updatedCount
        )

        return SettlementBatchSummary(
            batchId = savedBatch.id,
            batchRef = savedBatch.batchRef,
            merchantId = savedBatch.merchantId,
            totalAmount = savedBatch.totalAmount.amount,
            totalFee = savedBatch.totalFee.amount,
            netAmount = savedBatch.netAmount.amount,
            currency = savedBatch.totalAmount.currency.code,
            transactionCount = updatedCount,
            message = "Settlement batch created successfully"
        )
    }
}