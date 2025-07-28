package com.paymentsystem.core.presentation.mappers

import com.paymentsystem.core.application.commands.SettleTransactionsCommand
import com.paymentsystem.core.presentation.request.SettleTransactionsRequest
import org.springframework.stereotype.Component

@Component
class SettleTransactionsRequestMapper {
    fun toCommand(request: SettleTransactionsRequest): SettleTransactionsCommand {
        return SettleTransactionsCommand(
            merchantId = request.merchantId,
            limit = request.limit
        )
    }
}
