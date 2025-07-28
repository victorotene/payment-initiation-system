package com.paymentsystem.core.application.interfaces

import com.paymentsystem.core.application.dto.transferservice.TransferRequest
import com.paymentsystem.core.application.dto.transferservice.TransferResponse

interface FundTransfer {
    suspend fun initiateTransfer(command: TransferRequest): TransferResponse
}
