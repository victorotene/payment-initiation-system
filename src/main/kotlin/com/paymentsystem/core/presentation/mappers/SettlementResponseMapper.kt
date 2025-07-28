package com.paymentsystem.core.presentation.mappers

import com.paymentsystem.core.application.dto.SettlementBatchSummary
import com.paymentsystem.core.presentation.response.SettlementResponse
import org.springframework.stereotype.Component

@Component
class SettlementResponseMapper {
    fun fromSummary(summary: SettlementBatchSummary): SettlementResponse {
        return SettlementResponse(
            batchId = summary.batchId,
            batchRef = summary.batchRef,
            merchantId = summary.merchantId,
            totalAmount = summary.totalAmount,
            totalFee = summary.totalFee,
            netAmount = summary.netAmount,
            currency = summary.currency,
            transactionCount = summary.transactionCount,
            message = summary.message
        )
    }
}
