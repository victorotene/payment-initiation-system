package com.paymentsystem.core.presentation.mappers

import com.paymentsystem.core.application.commands.CreateMerchantCommand
import com.paymentsystem.core.presentation.request.CreateMerchantRequest

fun CreateMerchantRequest.toCommand(): CreateMerchantCommand {
    return CreateMerchantCommand(
        businessName = this.businessName,
        email = this.email,
        settlementAccount = this.settlementAccount,
        balance = this.balance
    )
}
