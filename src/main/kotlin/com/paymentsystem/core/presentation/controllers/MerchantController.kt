package com.paymentsystem.core.presentation.controllers

import com.paymentsystem.core.application.commands.CreateMerchantCommand
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.presentation.mappers.CreateMerchantResponseMapper
import com.paymentsystem.core.presentation.mappers.toCommand
import com.paymentsystem.core.presentation.request.CreateMerchantRequest
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
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
    fun createMerchant(@Valid @RequestBody request: CreateMerchantRequest): ResponseEntity<Any> = runBlocking {
        logger.info("Received CreateMerchantRequest: {}", request)

        val command = request.toCommand()
        val result = commandBus.send(command)

        val response = CreateMerchantResponseMapper.fromResult(result)

        logger.info("Successfully created merchant with ID: {}", response.id)

        ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}