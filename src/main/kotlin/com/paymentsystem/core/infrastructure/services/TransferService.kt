package com.paymentsystem.core.infrastructure.services

import com.paymentsystem.core.application.dto.transferservice.TransferRequest
import com.paymentsystem.core.application.dto.transferservice.TransferResponse
import com.paymentsystem.core.application.interfaces.FundTransfer
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class TransferService : FundTransfer {
    override suspend fun initiateTransfer(command: TransferRequest): TransferResponse {
        delay(500)

        println("Mock transferring ${command.amount} ${command.currency} from ${command.senderAccountId} to ${command.recipientAccountId}")

        return TransferResponse(
            transactionId = "mock-${command.amount}",
            status = "SUCCESS",
            transactionCode = "00",
            timestamp = System.currentTimeMillis()
        )
    }
}

