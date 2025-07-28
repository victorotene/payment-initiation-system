package com.paymentsystem.core.application.commands

import com.paymentsystem.core.application.dto.TransactionResult
import com.paymentsystem.core.application.interfaces.Command
import java.math.BigDecimal
import java.util.UUID

data class InitiateTransactionCommand(
    val merchantId: UUID,
    val merchantRef: String,
    val amount: BigDecimal,
    val currency: String,
    val idempotencyKey: String
) : Command<TransactionResult>

