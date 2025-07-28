package com.paymentsystem.core.UnitTest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.paymentsystem.core.application.commands.CreateMerchantCommand
import com.paymentsystem.core.application.dto.CreateMerchantResult
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.domain.enums.MerchantStatus
import com.paymentsystem.core.presentation.controllers.MerchantController
import com.paymentsystem.core.presentation.request.CreateMerchantRequest
import io.mockk.coEvery
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*

/*@WebMvcTest(MerchantController::class)
class MerchantControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var commandBus: CommandBus

    @Test
    fun `should return 201 CREATED with ApiResponse when merchant is created`() { // Updated test name
        val request = CreateMerchantRequest(
            businessName = "Test Biz",
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

        coEvery { commandBus.send(any<CreateMerchantCommand>()) } returns result

        mockMvc.post("/api/v1/accountcreation") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            // Assert on the ApiResponse fields
            jsonPath("$.success", `is`(true))
            jsonPath("$.code", `is`("CREATED"))
            jsonPath("$.message", `is`("Merchant created successfully"))
            // Assert on the data field (which contains the MerchantResponse)
            jsonPath("$.data.id", notNullValue())
            jsonPath("$.data.businessName", `is`("Test Biz"))
            jsonPath("$.data.email", `is`("test@example.com"))
            jsonPath("$.data.settlementAccount", `is`("1234567890"))
            jsonPath("$.data.balance", `is`(1000.00))
            jsonPath("$.data.status", `is`("ACTIVE"))
        }
    }
}*/
