package com.paymentsystem.core.domain.interfaces.services

import com.paymentsystem.core.domain.valueobjects.Money
import java.util.UUID

interface BalanceService {
    suspend fun checkBalance(merchantId: UUID, requiredAmount: Money): Boolean
    suspend fun reserveBalance(merchantId: UUID, amount: Money): Boolean
    suspend fun confirmReservation(merchantId: UUID, amount: Money): Boolean
    suspend fun releaseReservation(merchantId: UUID, amount: Money): Boolean
    suspend fun getCurrentBalance(merchantId: UUID): Money?
}