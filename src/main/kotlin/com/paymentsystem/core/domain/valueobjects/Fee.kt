package com.paymentsystem.core.domain.valueobjects

import java.math.BigDecimal

data class Fee(
    val percentage: BigDecimal,
    val maxCap: Money?
) {
    init {
        require(percentage >= BigDecimal.ZERO && percentage <= BigDecimal.ONE) {
            "Fee percentage must be between 0 and 1"
        }
    }

    fun calculate(amount: Money): Money {
        val feeAmount = amount.multiply(percentage)
        return if (maxCap != null && feeAmount.isGreaterThan(maxCap)) {
            maxCap
        } else {
            feeAmount
        }
    }
}