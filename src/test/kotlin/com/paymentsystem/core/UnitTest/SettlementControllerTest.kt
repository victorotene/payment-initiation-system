package com.paymentsystem.core.UnitTest

import com.paymentsystem.core.application.commands.SettleTransactionsCommand
import com.paymentsystem.core.application.dto.SettlementBatchSummary
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.presentation.controllers.SettlementController
import com.paymentsystem.core.presentation.mappers.SettlementResponseMapper
import com.paymentsystem.core.presentation.mappers.SettleTransactionsRequestMapper
import com.paymentsystem.core.presentation.request.SettleTransactionsRequest
import com.paymentsystem.core.presentation.response.ApiResponse
import com.paymentsystem.core.presentation.response.SettlementResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.*

class SettlementControllerTest {

    private lateinit var commandBus: CommandBus
    private lateinit var requestMapper: SettleTransactionsRequestMapper
    private lateinit var responseMapper: SettlementResponseMapper
    private lateinit var controller: SettlementController

    @BeforeEach
    fun setup() {
        commandBus = mock()
        requestMapper = mock()
        responseMapper = mock()
        controller = SettlementController(commandBus, responseMapper, requestMapper)
    }

    @Test
    fun `should settle transactions successfully`() = runBlocking {
        // Arrange
        val merchantId = UUID.randomUUID()
        val batchId = UUID.randomUUID()
        val batchRef = "BATCH12345"

        val request = SettleTransactionsRequest(merchantId = merchantId, limit = 100)
        val command = SettleTransactionsCommand(merchantId = merchantId, limit = 100)

        val summary = SettlementBatchSummary(
            batchId = batchId,
            batchRef = batchRef,
            merchantId = merchantId,
            totalAmount = BigDecimal("1000.00"),
            totalFee = BigDecimal("50.00"),
            netAmount = BigDecimal("950.00"),
            currency = "NGN",
            transactionCount = 5,
            message = "Settlement completed"
        )

        val response = SettlementResponse(
            batchId = batchId,
            batchRef = batchRef,
            merchantId = merchantId,
            totalAmount = BigDecimal("1000.00"),
            totalFee = BigDecimal("50.00"),
            netAmount = BigDecimal("950.00"),
            currency = "NGN",
            transactionCount = 5,
            message = "Settlement completed"
        )

        whenever(requestMapper.toCommand(request)).thenReturn(command)
        whenever(commandBus.send(command)).thenReturn(summary)
        whenever(responseMapper.fromSummary(summary)).thenReturn(response)

        // Act
        val result = controller.settleTransactions(request)

        // Assert
        assertEquals(HttpStatus.OK, result.statusCode)

        val body = result.body as ApiResponse<SettlementResponse>
        assertEquals("Transactions settled successfully", body.message)
        assertEquals(true, true)

        val data = body.data!!
        assertEquals(batchId, data.batchId)
        assertEquals(batchRef, data.batchRef)
        assertEquals(merchantId, data.merchantId)
        assertEquals(BigDecimal("950.00"), data.netAmount)
        assertEquals("NGN", data.currency)
        assertEquals("Settlement completed", data.message)
    }
}
