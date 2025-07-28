package com.paymentsystem.core.presentation.controllers

import com.paymentsystem.core.application.commands.InitiateTransactionCommand
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.application.interfaces.QueryBus
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.exceptions.*
import com.paymentsystem.core.presentation.mappers.*
import com.paymentsystem.core.presentation.request.InitiateTransactionRequest
import com.paymentsystem.core.presentation.response.ApiResponse
import com.paymentsystem.core.presentation.response.InitiateTransactionResponse
import com.paymentsystem.core.presentation.response.TransactionListResponse
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
    ): ResponseEntity<ApiResponse<InitiateTransactionResponse>> {
        logger.info("Received InitiateTransactionRequest: {}", request)

        return try {
            val command: InitiateTransactionCommand = requestMapper.toCommand(request)
            val result = commandBus.send(command)
            val responseBody: InitiateTransactionResponse = responseMapper.fromResult(result)

            logger.info("Transaction successfully initiated with ID: {}", result.id)
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                    data = responseBody,
                    message = "Transaction initiated successfully"
                )
            )

        } catch (e: MerchantNotFoundException) {
            logger.warn("Merchant not found: {}", e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.failure(message = "Merchant not found")
            )

        } catch (e: MerchantInactiveException) {
            logger.warn("Merchant is inactive: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failure(message = "Merchant is inactive")
            )

        } catch (e: InsufficientFundsException) {
            logger.warn("Insufficient funds: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failure(message = "Insufficient balance")
            )

        } catch (e: TransactionAlreadyExistsException) {
            logger.warn("Duplicate transaction: {}", e.message)
            ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.failure(message = "Duplicate transaction")
            )

        } catch (e: AccountSuspendedException) {
            logger.warn("Account suspended: {}", e.message)
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.failure(message = "Account suspended")
            )

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid input: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failure(message = "Invalid input")
            )

        } catch (e: Exception) {
            logger.error("Unexpected error occurred: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.failure(message = "Internal server error")
            )
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
    ): ResponseEntity<ApiResponse<TransactionListResponse>> {
        logger.info(
            "Received ListTransactionsRequest for merchantId={}, filters: status={}, fromDate={}, toDate={}, page={}, size={}",
            merchantId, status, fromDate, toDate, page, size
        )

        return try {
            val query = TransactionsListQueryMapper.toQuery(merchantId, status, fromDate, toDate, page, size)
            val result = queryBus.send(query)
            val responseBody = TransactionListResponseMapper.fromResult(result)

            logger.info("Successfully retrieved {} transactions for merchant: {}", responseBody.transactions.size, merchantId)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = responseBody,
                    message = "Transactions retrieved successfully"
                )
            )

        } catch (e: MerchantNotFoundException) {
            logger.warn("Merchant not found: {}", e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.failure(message = "Merchant not found")
            )

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid request parameters: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failure(message = "Invalid request parameters")
            )

        } catch (e: Exception) {
            logger.error("Unexpected error while listing transactions: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.failure(message = "Internal server error")
            )
        }
    }
}
