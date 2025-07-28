package com.paymentsystem.core.presentation.controllers

import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.domain.exceptions.MerchantNotFoundException
import com.paymentsystem.core.domain.exceptions.NoUnsettledTransactionsException
import com.paymentsystem.core.presentation.mappers.SettlementResponseMapper
import com.paymentsystem.core.presentation.mappers.SettleTransactionsRequestMapper
import com.paymentsystem.core.presentation.request.SettleTransactionsRequest
import com.paymentsystem.core.presentation.response.ApiResponse
import com.paymentsystem.core.presentation.response.SettlementResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
    ): ResponseEntity<ApiResponse<SettlementResponse>> {
        logger.info(
            "Received SettleTransactionsRequest for merchantId={}, limit={}",
            request.merchantId,
            request.limit
        )

        return try {
            val command = requestMapper.toCommand(request)
            val result = commandBus.send(command)

            val response = responseMapper.fromSummary(result)
            logger.info("Successfully settled {} transactions into batch {}", response.transactionCount, response.batchId)

            ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                    data = response,
                    message = "Transactions settled successfully"
                )
            )

        } catch (e: MerchantNotFoundException) {
            logger.warn("Merchant not found: {}", e.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.failure(message = e.message ?: "Merchant not found")
            )

        } catch (e: NoUnsettledTransactionsException) {
            logger.warn("No unsettled transactions: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failure(message = e.message ?: "No unsettled transactions to settle")
            )

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid input: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.failure(message = e.message ?: "Invalid input")
            )

        } catch (e: Exception) {
            logger.error("Unexpected error during settlement: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.failure(message = "Internal server error")
            )
        }
    }
}
