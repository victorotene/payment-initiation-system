package com.paymentsystem.core.domain.interfaces.repository

import com.paymentsystem.core.domain.Merchant
import java.util.UUID

interface MerchantRepository {
    suspend fun save(merchant: Merchant): Merchant
    suspend fun findById(id: UUID): Merchant?
    suspend fun findByEmail(email: String): Merchant?
    suspend fun existsByEmail(email: String): Boolean
}



//suspend fun updateBalances(merchantId: UUID, balanceChange: BigDecimal, lockedBalanceChange: BigDecimal)
//suspend fun incrementFailedAttempts(merchantId: UUID)
//suspend fun resetFailedAttempts(merchantId: UUID)
//suspend fun deactivateMerchant(merchantId: UUID)