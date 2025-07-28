package com.paymentsystem.core.application.commands

import com.paymentsystem.core.application.dto.SettlementBatchSummary
import com.paymentsystem.core.application.interfaces.Command
import java.util.UUID

data class SettleTransactionsCommand(
    val merchantId: UUID,
    val limit: Int = 100
) : Command<SettlementBatchSummary>
