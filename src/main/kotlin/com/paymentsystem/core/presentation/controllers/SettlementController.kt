package com.paymentsystem.core.presentation.controllers

import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.presentation.mappers.SettlementResponseMapper
import com.paymentsystem.core.presentation.mappers.SettleTransactionsRequestMapper
import com.paymentsystem.core.presentation.request.SettleTransactionsRequest
import com.paymentsystem.core.presentation.response.SettlementResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/settlements")
class SettlementController(
    private val commandBus: CommandBus,
    private val responseMapper: SettlementResponseMapper,
    private val requestMapper: SettleTransactionsRequestMapper
) {
    private val logger = LoggerFactory.getLogger(SettlementController::class.java)

    @PostMapping
    suspend fun settleTransactions(
        @Valid @RequestBody request: SettleTransactionsRequest
    ): ResponseEntity<SettlementResponse> {
        logger.info("Received request to settle transactions for merchant: {}, limit: {}", request.merchantId, request.limit)

        val command = requestMapper.toCommand(request)
        val result = commandBus.send(command)

        val response: SettlementResponse = responseMapper.fromSummary(result)

        logger.info("Successfully settled {} transactions into batch {}", response.transactionCount, response.batchId)

        return ResponseEntity.ok(response)
    }
}
