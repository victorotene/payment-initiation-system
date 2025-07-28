package com.paymentsystem.core.domain.valueobjects

@JvmInline
value class EmailAddress(val value: String) {
    init {
        require(value.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) { "Invalid email address format: $value" }
    }
}
