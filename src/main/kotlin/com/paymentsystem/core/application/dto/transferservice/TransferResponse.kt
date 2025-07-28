package com.paymentsystem.core.application.dto.transferservice

data class TransferResponse(
    val transactionId: String,
    val transactionCode: String,
    val status: String,
    val timestamp: Long,
    val message: String = ""
)