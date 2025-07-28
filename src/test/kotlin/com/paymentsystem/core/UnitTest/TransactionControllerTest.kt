package com.paymentsystem.core.UnitTest

import TransactionListResponseMapper
import com.paymentsystem.core.application.commands.InitiateTransactionCommand
import com.paymentsystem.core.application.dto.TransactionListResult
import com.paymentsystem.core.application.dto.TransactionResult
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.application.interfaces.QueryBus
import com.paymentsystem.core.application.queries.ListTransactionsQuery
import com.paymentsystem.core.domain.enums.DebitStatus
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.exceptions.MerchantNotFoundException
import com.paymentsystem.core.presentation.controllers.TransactionController
import com.paymentsystem.core.presentation.mappers.InitiateTransactionRequestMapper
import com.paymentsystem.core.presentation.mappers.InitiateTransactionResponseMapper
import com.paymentsystem.core.presentation.request.InitiateTransactionRequest
import com.paymentsystem.core.presentation.response.InitiateTransactionResponse
import com.paymentsystem.core.presentation.response.TransactionListResponse
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransactionControllerTest {

    private lateinit var commandBus: CommandBus
    private lateinit var queryBus: QueryBus
    private lateinit var requestMapper: InitiateTransactionRequestMapper
    private lateinit var responseMapper: InitiateTransactionResponseMapper
    private lateinit var controller: TransactionController

    @BeforeEach
    fun setUp() {
        commandBus = mockk()
        queryBus = mockk()
        requestMapper = mockk()
        responseMapper = mockk()
        controller = TransactionController(commandBus, queryBus, requestMapper, responseMapper)
    }

    @Test
    fun `initiateTransaction returns 201 CREATED on success`() = runBlocking {
        val now = ZonedDateTime.now()
        val merchantId = UUID.randomUUID()
        val settlementBatchId = UUID.randomUUID()

        val request = InitiateTransactionRequest(
            merchantId = merchantId,
            merchantRef = "INV123",
            amount = BigDecimal("5000.00"),
            currency = "NGN",
            idempotencyKey = "unique-key-001"
        )

        val command = mockk<InitiateTransactionCommand>()
        val domainResult = TransactionResult(
            id = UUID.randomUUID(),
            merchantId = merchantId,
            merchantRef = request.merchantRef,
            internalRef = "INT-456",
            amount = request.amount,
            currency = request.currency,
            fee = BigDecimal("50.00"),
            netAmount = BigDecimal("4950.00"),
            status = TransactionStatus.SUCCESS,
            customerSimulatedDebitStatus = DebitStatus.DEBITED,
            retryCount = 0,
            settlementBatchId = settlementBatchId,
            createdAt = now,
            updatedAt = now,
            message = "",
            idempotencyKey = request.idempotencyKey
        )

        val response = InitiateTransactionResponse(
            id = domainResult.id,
            merchantId = domainResult.merchantId,
            merchantRef = domainResult.merchantRef,
            amount = domainResult.amount,
            currency = domainResult.currency,
            fee = domainResult.fee,
            netAmount = domainResult.netAmount,
            status = domainResult.status.name,
            customerSimulatedDebitStatus = domainResult.customerSimulatedDebitStatus.name,
            idempotencyKey = domainResult.idempotencyKey,
            createdAt = domainResult.createdAt
        )

        every { requestMapper.toCommand(request) } returns command
        coEvery { commandBus.send(command) } returns domainResult
        every { responseMapper.fromResult(domainResult) } returns response

        val result = controller.initiateTransaction(request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertNotNull(result.body)
        assertEquals(true, result.body!!.success)
        assertEquals("Transaction initiated successfully", result.body!!.message)
        assertEquals(response.id, result.body!!.data?.id)
    }

    @Test
    fun `initiateTransaction returns 404 NOT_FOUND when merchant is not found`() = runBlocking {
        val request = mockk<InitiateTransactionRequest>(relaxed = true)
        every { requestMapper.toCommand(request) } throws MerchantNotFoundException("Merchant not found")

        val result = controller.initiateTransaction(request)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        assertNotNull(result.body)
        assertEquals(false, result.body!!.success)
        assertEquals("Merchant not found", result.body!!.message)
        assertEquals(null, result.body!!.data)
    }

    @Test
    fun `listTransactions returns 200 OK with correct paginated response`() = runBlocking {
        val merchantId = UUID.randomUUID()
        val now = ZonedDateTime.now()
        val page = 1
        val size = 2

        val query = ListTransactionsQuery(
            merchantId = merchantId,
            status = null,
            fromDate = null,
            toDate = null,
            page = page,
            size = size
        )

        val transactionListResult = TransactionListResult(
            transactions = listOf(
                TransactionResult(
                    id = UUID.randomUUID(),
                    merchantId = merchantId,
                    merchantRef = "REF001",
                    internalRef = "INT001",
                    amount = BigDecimal("1000.00"),
                    currency = "NGN",
                    fee = BigDecimal("10.00"),
                    netAmount = BigDecimal("990.00"),
                    status = TransactionStatus.SUCCESS,
                    customerSimulatedDebitStatus = DebitStatus.DEBITED,
                    retryCount = 1,
                    settlementBatchId = null,
                    createdAt = now,
                    updatedAt = now,
                    message = "",
                    idempotencyKey = "idem-1"
                ),
                TransactionResult(
                    id = UUID.randomUUID(),
                    merchantId = merchantId,
                    merchantRef = "REF002",
                    internalRef = "INT002",
                    amount = BigDecimal("500.00"),
                    currency = "NGN",
                    fee = BigDecimal("5.00"),
                    netAmount = BigDecimal("495.00"),
                    status = TransactionStatus.SUCCESS,
                    customerSimulatedDebitStatus = DebitStatus.DEBITED,
                    retryCount = 0,
                    settlementBatchId = UUID.randomUUID(),
                    createdAt = now,
                    updatedAt = now,
                    message = "",
                    idempotencyKey = "idem-2"
                )
            ),
            totalElements = 2,
            totalPages = 1,
            currentPage = page,
            pageSize = size,
            hasNext = false,
            hasPrevious = false
        )

        val expectedResponse = TransactionListResponseMapper.fromResult(transactionListResult)
        mockkObject(TransactionListResponseMapper)

        coEvery { queryBus.send(query) } returns transactionListResult
        every { TransactionListResponseMapper.fromResult(transactionListResult) } returns expectedResponse

        val response = controller.listTransactions(
            merchantId = merchantId,
            status = null,
            fromDate = null,
            toDate = null,
            page = page,
            size = size
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.success)
        assertEquals("Transactions retrieved successfully", response.body?.message)
        assertEquals(expectedResponse, response.body?.data)
    }
}
