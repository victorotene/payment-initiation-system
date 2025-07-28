package com.paymentsystem.core.UnitTest

/*import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.paymentsystem.core.application.commands.CreateMerchantCommand
import com.paymentsystem.core.application.dto.CreateMerchantResult
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.domain.enums.MerchantStatus
import com.paymentsystem.core.presentation.controllers.MerchantController
import com.paymentsystem.core.presentation.request.CreateMerchantRequest
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*

@WebMvcTest(MerchantController::class)
class MerchantControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var commandBus: CommandBus

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return 201 Created when merchant is created`() {
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
            status { isCreated() } // Changed from isOk() to isCreated()
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id").exists()
            jsonPath("$.businessName").value("Test Biz")
            jsonPath("$.email").value("test@example.com")
            jsonPath("$.settlementAccount").value("1234567890")
            jsonPath("$.balance").value(1000.00)
            jsonPath("$.status").value("ACTIVE")
        }.andDo {
            print()
        }
    }
}*/