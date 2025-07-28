package com.paymentsystem.core.presentation.mappers

import com.paymentsystem.core.application.dto.CreateMerchantResult
import com.paymentsystem.core.presentation.response.MerchantResponse

object CreateMerchantResponseMapper {
    fun fromResult(result: CreateMerchantResult): MerchantResponse {
        return MerchantResponse(
            id = result.id,
            businessName = result.businessName,
            email = result.email,
            settlementAccount = result.settlementAccount,
            balance = result.balance,
            status = result.status,
            createdAt = result.createdAt
        )
    }
}
