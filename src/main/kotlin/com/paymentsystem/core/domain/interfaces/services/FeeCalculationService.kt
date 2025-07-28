package com.paymentsystem.core.domain.interfaces.services

import com.paymentsystem.core.domain.valueobjects.Money

interface FeeCalculationService {
    fun calculateFee(amount: Money): Money
}