package com.paymentsystem.core.presentation.mappers

import com.paymentsystem.core.application.commands.InitiateTransactionCommand
import com.paymentsystem.core.presentation.request.InitiateTransactionRequest
import org.springframework.stereotype.Component

@Component
class InitiateTransactionRequestMapper {
    fun toCommand(request: InitiateTransactionRequest): InitiateTransactionCommand {
        return InitiateTransactionCommand(
            merchantId = request.merchantId,
            merchantRef = request.merchantRef,
            amount = request.amount,
            currency = request.currency,
            idempotencyKey = request.idempotencyKey
        )
    }
}
