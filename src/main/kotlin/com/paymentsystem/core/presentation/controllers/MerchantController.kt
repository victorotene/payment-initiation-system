package com.paymentsystem.core.presentation.controllers

import com.paymentsystem.core.application.commands.CreateMerchantCommand
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.domain.exceptions.MerchantAlreadyExistsException
import com.paymentsystem.core.presentation.response.ErrorResponse
import com.paymentsystem.core.presentation.mappers.CreateMerchantResponseMapper
import com.paymentsystem.core.presentation.mappers.toCommand
import com.paymentsystem.core.presentation.request.CreateMerchantRequest
import com.paymentsystem.core.presentation.response.MerchantResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/accountcreation")
class MerchantController(
    private val commandBus: CommandBus
) {
    private val logger = LoggerFactory.getLogger(MerchantController::class.java)

    @PostMapping
    suspend fun createMerchant(@Valid @RequestBody request: CreateMerchantRequest): ResponseEntity<Any> {
        logger.info("Received CreateMerchantRequest: {}", request)

        return try {
            val command = request.toCommand()
            val result = commandBus.send(command)

            val response = CreateMerchantResponseMapper.fromResult(result)

            logger.info("Successfully created merchant with ID: {}", response.id)

            ResponseEntity.status(HttpStatus.CREATED)
                .body(response)

        } catch (e: MerchantAlreadyExistsException) {
            logger.warn("Merchant already exists: {}", e.message)
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse(e.message ?: "Merchant already exists", "MERCHANT_ALREADY_EXISTS"))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid merchant input: {}", e.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(e.message ?: "Invalid input", "INVALID_INPUT"))

        } catch (e: Exception) {
            logger.error("Unexpected error occurred: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("Internal server error", "INTERNAL_ERROR"))
        }
    }
}
