package com.paymentsystem.core.presentation.controllers

import com.paymentsystem.core.application.commands.InitiateTransactionCommand
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.application.interfaces.QueryBus
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.exceptions.*
import com.paymentsystem.core.presentation.mappers.*
import com.paymentsystem.core.presentation.request.InitiateTransactionRequest
import com.paymentsystem.core.presentation.response.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/v1/transactions")
class TransactionController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus,
    private val requestMapper: InitiateTransactionRequestMapper,
    private val responseMapper: InitiateTransactionResponseMapper
) {
    private val logger = LoggerFactory.getLogger(TransactionController::class.java)

    @PostMapping("/initiate")
    suspend fun initiateTransaction(
        @Valid @RequestBody request: InitiateTransactionRequest
    ): ResponseEntity<Any> {
        logger.info("Received InitiateTransactionRequest: {}", request)

        return try {
            val command: InitiateTransactionCommand = requestMapper.toCommand(request)
            val result = commandBus.send(command)
            val response: InitiateTransactionResponse = responseMapper.fromResult(result)

            logger.info("Transaction successfully initiated with ID: {}", result.id)
            ResponseEntity.status(HttpStatus.CREATED).body(response)

        } catch (e: MerchantNotFoundException) {
            logger.warn("Merchant not found: {}", e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse(e.message ?: "Merchant not found", "MERCHANT_NOT_FOUND"))

        } catch (e: MerchantInactiveException) {
            logger.warn("Merchant is inactive: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(e.message ?: "Merchant is inactive", "MERCHANT_INACTIVE"))

        } catch (e: InsufficientFundsException) {
            logger.warn("Insufficient funds: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(e.message ?: "Insufficient balance", "INSUFFICIENT_BALANCE"))

        } catch (e: TransactionAlreadyExistsException) {
            logger.warn("Duplicate transaction: {}", e.message)
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse(e.message ?: "Duplicate transaction", "DUPLICATE_TRANSACTION"))

        } catch (e: AccountSuspendedException) {
            logger.warn("Account suspended: {}", e.message)
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse(e.message ?: "Account suspended", "ACCOUNT_SUSPENDED"))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid input: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(e.message ?: "Invalid input", "INVALID_INPUT"))

        } catch (e: Exception) {
            logger.error("Unexpected error occurred: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("Internal server error", "INTERNAL_ERROR"))
        }
    }

    @GetMapping("{merchantId}/list")
    suspend fun listTransactions(
        @PathVariable merchantId: UUID,
        @RequestParam(required = false) status: TransactionStatus? = null,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: LocalDateTime? = null,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) toDate: LocalDateTime? = null,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int
    ): ResponseEntity<Any> {
        logger.info("Received ListTransactionsRequest for merchant: {} with filters: status={}, fromDate={}, toDate={}, page={}, size={}",
            merchantId, status, fromDate, toDate, page, size)

        return try {
            val query = TransactionsListQueryMapper.toQuery(merchantId, status, fromDate, toDate, page, size)
            val result = queryBus.send(query)
            val response: TransactionListResponse = TransactionListResponseMapper.fromResult(result)

            logger.info("Successfully retrieved {} transactions for merchant: {}", response.transactions.size, merchantId)
            ResponseEntity.ok(response)

        } catch (e: MerchantNotFoundException) {
            logger.warn("Merchant not found: {}", e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse(e.message ?: "Merchant not found", "MERCHANT_NOT_FOUND"))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid request parameters: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(e.message ?: "Invalid request parameters", "INVALID_PARAMETERS"))

        } catch (e: Exception) {
            logger.error("Unexpected error while listing transactions: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("Internal server error", "INTERNAL_ERROR"))
        }
    }
}
