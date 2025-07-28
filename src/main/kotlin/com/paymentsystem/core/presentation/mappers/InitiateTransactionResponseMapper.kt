package com.paymentsystem.core.presentation.mappers

import com.paymentsystem.core.application.dto.TransactionResult
import com.paymentsystem.core.presentation.response.InitiateTransactionResponse
import org.springframework.stereotype.Component

@Component
class InitiateTransactionResponseMapper {
    fun fromResult(result: TransactionResult): InitiateTransactionResponse {
        return InitiateTransactionResponse(
            id = result.id,
            merchantId = result.merchantId,
            merchantRef = result.merchantRef,
            amount = result.amount,
            currency = result.currency,
            fee = result.fee,
            netAmount = result.netAmount,
            status = result.status.name,
            customerSimulatedDebitStatus = result.customerSimulatedDebitStatus.name,
            idempotencyKey = result.idempotencyKey.toString(),
            createdAt = result.createdAt,
            //message = result.message
        )
    }
}
