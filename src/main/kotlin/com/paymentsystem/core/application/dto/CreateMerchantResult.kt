package com.paymentsystem.core.application.dto

import com.paymentsystem.core.domain.Merchant
import com.paymentsystem.core.domain.enums.MerchantStatus
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class CreateMerchantResult(
    val id: UUID,
    val businessName: String,
    val email: String,
    val settlementAccount: String,
    val status: MerchantStatus,
    val balance: BigDecimal,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(merchant: Merchant): CreateMerchantResult {
            return CreateMerchantResult(
                id = merchant.id,
                businessName = merchant.businessName,
                email = merchant.email.value,
                settlementAccount = merchant.settlementAccount,
                balance = merchant.balance,
                status = merchant.status,
                createdAt = merchant.createdAt
            )
        }
    }
}
