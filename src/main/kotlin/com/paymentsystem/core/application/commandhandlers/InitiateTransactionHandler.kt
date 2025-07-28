package com.paymentsystem.core.application.handlers

import com.paymentsystem.core.application.commands.InitiateTransactionCommand
import com.paymentsystem.core.application.dto.TransactionResult
import com.paymentsystem.core.application.dto.transferservice.TransferRequest
import com.paymentsystem.core.application.interfaces.CommandHandler
import com.paymentsystem.core.application.interfaces.FundTransfer
import com.paymentsystem.core.domain.Transaction
import com.paymentsystem.core.domain.enums.Currency
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.exceptions.*
import com.paymentsystem.core.domain.interfaces.repository.MerchantRepository
import com.paymentsystem.core.domain.interfaces.repository.TransactionRepository
import com.paymentsystem.core.domain.interfaces.services.FeeCalculationService
import com.paymentsystem.core.domain.valueobjects.Money
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class InitiateTransactionHandler(
    private val merchantRepository: MerchantRepository,
    private val transactionRepository: TransactionRepository,
    private val fundTransfer: FundTransfer,
    private val feeCalculator: FeeCalculationService,
) : CommandHandler<InitiateTransactionCommand, TransactionResult> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun handle(command: InitiateTransactionCommand): TransactionResult {
        val currency = runCatching { Currency.valueOf(command.currency.uppercase()) }
            .getOrElse { throw InvalidCurrencyException("Invalid currency: ${command.currency}") }

        transactionRepository.findByIdempotencyKey(command.idempotencyKey)?.let { existingTxn ->
            return TransactionResult.fromTransaction(existingTxn, "Transaction already exists")
        }

        val merchant = merchantRepository.findById(command.merchantId)
            ?: throw MerchantNotFoundException("Merchant ${command.merchantId} not found")
        if (!merchant.isActive()) {
            throw AccountSuspendedException("Your account ${merchant.id} is suspended")
        }

        val amount = Money(command.amount, currency)
        val fee = feeCalculator.calculateFee(amount)
        val totalToReserve = fee.add(amount)

        val updatedMerchant = try {
            merchant.reserve(totalToReserve.amount)
        } catch (ex: IllegalArgumentException) {
            val failedMerchant = merchant.incrementFailedAttempts()
            merchantRepository.save(failedMerchant)

            val failedTransaction = Transaction.createFailed(
                idempotencyKey = command.idempotencyKey,
                merchantId = merchant.id,
                amount = amount,
                fee = fee,
            )
            transactionRepository.save(failedTransaction)
            throw InsufficientFundsException("Insufficient funds for merchant ${merchant.id}")
        }

        val transaction = Transaction.initiate(
            idempotencyKey = command.idempotencyKey,
            merchantId = merchant.id,
            amount = amount,
            fee = fee,
            merchantRef = command.merchantRef
        )
        val savedTransaction = transactionRepository.save(transaction)

        val transferResult = fundTransfer.initiateTransfer(
            TransferRequest(
                amount = amount.amount,
                currency = currency.toString(),
                senderAccountId = merchant.settlementAccount,
                recipientAccountId = "",
                reference = savedTransaction.internalRef,
            )
        )

        val (finalMerchant, updatedTransaction) = when (transferResult.transactionCode) {
            "00" -> {
                val completedTxn = savedTransaction.complete(success = true)
                val finalMerchant = updatedMerchant.debit(totalToReserve.amount)
                merchantRepository.save(finalMerchant)
                Pair(finalMerchant, completedTxn)
            }

            "01", "11" -> {
                logger.warn("Transfer code ${transferResult.transactionCode} for transaction: ${savedTransaction.id}")
                merchantRepository.save(updatedMerchant)
                val retriedTxn = savedTransaction.incrementRetryCount()
                Pair(updatedMerchant, retriedTxn)
            }

            else -> {
                logger.error("Unknown transfer code: ${transferResult.transactionCode} for transaction: ${savedTransaction.id}")
                merchantRepository.save(updatedMerchant)
                val retriedTxn = savedTransaction.incrementRetryCount()
                Pair(updatedMerchant, retriedTxn)
            }
        }

        //merchantRepository.save(finalMerchant)
        transactionRepository.update(updatedTransaction)

        val message = when (updatedTransaction.status) {
            TransactionStatus.SUCCESS -> "Transaction completed successfully"
            TransactionStatus.PENDING -> "Transaction pending"
            TransactionStatus.FAILED -> "Transaction failed"
            else -> "Transaction initiated"
        }

        return TransactionResult.fromTransaction(updatedTransaction, message)
    }
}