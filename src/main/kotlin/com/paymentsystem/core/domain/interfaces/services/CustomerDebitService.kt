package com.paymentsystem.core.domain.interfaces.services

import com.paymentsystem.core.domain.valueobjects.Money

interface CustomerDebitService {
    suspend fun simulateDebit(amount: Money, merchantRef: String, internalRef: String): Boolean
}