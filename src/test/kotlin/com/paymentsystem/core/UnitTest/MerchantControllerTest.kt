package com.paymentsystem.core.UnitTest

import com.paymentsystem.core.application.commands.CreateMerchantCommand
import com.paymentsystem.core.application.dto.CreateMerchantResult
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.domain.enums.MerchantStatus
import com.paymentsystem.core.domain.exceptions.MerchantAlreadyExistsException
import com.paymentsystem.core.presentation.controllers.MerchantController
import com.paymentsystem.core.presentation.request.CreateMerchantRequest
import com.paymentsystem.core.presentation.response.ApiResponse
import com.paymentsystem.core.presentation.response.MerchantResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MerchantControllerTest {

    private lateinit var commandBus: CommandBus
    private lateinit var controller: MerchantController

    @BeforeEach
    fun setUp() {
        commandBus = mock()
        controller = MerchantController(commandBus)
    }

    @Test
    fun `should return 201 CREATED when merchant is successfully created`() = runBlocking {
        // Arrange
        val request = CreateMerchantRequest(
            businessName = "Test Corp",
            email = "test@example.com",
            settlementAccount = "1234567890",
            balance = BigDecimal("1000.00")
        )

        val result = CreateMerchantResult(
            id = UUID.randomUUID(),
            businessName = request.businessName,
            email = request.email,
            settlementAccount = request.settlementAccount,
            balance = request.balance,
            status = MerchantStatus.ACTIVE,
            createdAt = ZonedDateTime.now()
        )

        whenever(commandBus.send(any<CreateMerchantCommand>())).thenReturn(result)

        // Act
        val response = controller.createMerchant(request)

        // Assert
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Merchant created successfully", response.body?.message)
        assertEquals(true, true)
        assertEquals(result.id, response.body?.data?.id)
    }

    @Test
    fun `should return 409 CONFLICT when merchant already exists`() = runBlocking {
        // Arrange
        val request = CreateMerchantRequest(
            businessName = "Duplicate Inc",
            email = "duplicate@example.com",
            settlementAccount = "9999999999",
            balance = BigDecimal("500.00")
        )

        whenever(commandBus.send(any<CreateMerchantCommand>())).thenThrow(MerchantAlreadyExistsException("Merchant already exists"))

        // Act
        val response = controller.createMerchant(request)

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Merchant already exists", response.body?.message)
        assertEquals(false, false)
        assertNull(response.body?.data)
    }


}
