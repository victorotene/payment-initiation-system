package com.paymentsystem.core.application.dto.transferservice

import java.math.BigDecimal

data class TransferRequest(
    val senderAccountId: String,
    val recipientAccountId: String,
    val amount: BigDecimal,
    val currency: String,
    val reference: String? = null
)