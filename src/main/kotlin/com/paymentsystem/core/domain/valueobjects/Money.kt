package com.paymentsystem.core.domain.valueobjects

import java.math.BigDecimal
import com.paymentsystem.core.domain.enums.Currency

data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
    }

    fun add(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies" }
        return Money(amount + other.amount, currency)
    }

    fun subtract(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract different currencies" }
        require(amount >= other.amount) { "Insufficient funds" }
        return Money(amount - other.amount, currency)
    }

    fun multiply(multiplier: BigDecimal): Money {
        return Money(amount * multiplier, currency)
    }

    fun isGreaterThan(other: Money): Boolean {
        require(currency == other.currency) { "Cannot compare different currencies" }
        return amount > other.amount
    }
}