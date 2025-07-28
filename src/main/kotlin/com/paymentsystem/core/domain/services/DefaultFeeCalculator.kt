package com.paymentsystem.core.domain.services

import com.paymentsystem.core.domain.interfaces.services.FeeCalculationService
import com.paymentsystem.core.domain.valueobjects.Money
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class DefaultFeeCalculator : FeeCalculationService {

    override fun calculateFee(amount: Money): Money {
        val feePercentage = BigDecimal("0.015")
        val maxCap = BigDecimal("200.00")
        val calculatedFee = amount.amount.multiply(feePercentage)
        val finalFee = if (calculatedFee > maxCap) maxCap else calculatedFee
        return Money(finalFee, amount.currency)
    }
}
