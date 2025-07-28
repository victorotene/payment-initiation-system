package com.paymentsystem.core.presentation.controllers

import com.paymentsystem.core.application.commands.InitiateTransactionCommand
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.application.interfaces.QueryBus
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.presentation.mappers.*
import com.paymentsystem.core.presentation.request.InitiateTransactionRequest
import com.paymentsystem.core.presentation.response.InitiateTransactionResponse
import com.paymentsystem.core.presentation.response.TransactionListResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
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
    ): ResponseEntity<InitiateTransactionResponse> {
        logger.info("Received InitiateTransactionRequest: {}", request)

        val command: InitiateTransactionCommand = requestMapper.toCommand(request)
        val result = commandBus.send(command)
        val response: InitiateTransactionResponse = responseMapper.fromResult(result)

        logger.info("Transaction successfully initiated with ID: {}", result.id)

        return ResponseEntity.status(201).body(response)
    }

    @GetMapping("{merchantId}/list")
    suspend fun listTransactions(
        @PathVariable merchantId: UUID,
        @RequestParam(required = false) status: TransactionStatus? = null,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: LocalDateTime? = null,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) toDate: LocalDateTime? = null,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int
    ): ResponseEntity<TransactionListResponse> {
        logger.info(
            "Received ListTransactionsRequest for merchant: {} with filters: status={}, fromDate={}, toDate={}, page={}, size={}",
            merchantId, status, fromDate, toDate, page, size
        )

        val query = TransactionsListQueryMapper.toQuery(merchantId, status, fromDate, toDate, page, size)
        val result = queryBus.send(query)
        val response: TransactionListResponse = TransactionListResponseMapper.fromResult(result)

        logger.info("Successfully retrieved {} transactions for merchant: {}", response.transactions.size, merchantId)

        return ResponseEntity.ok(response)
    }
}
