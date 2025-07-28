package com.paymentsystem.core.application.commands

import com.paymentsystem.core.application.dto.CreateMerchantResult
import com.paymentsystem.core.application.interfaces.Command
import com.paymentsystem.core.presentation.response.MerchantResponse
import java.math.BigDecimal

data class CreateMerchantCommand(
    val businessName: String,
    val email: String,
    val settlementAccount: String,
    val balance: BigDecimal
) : Command<CreateMerchantResult>