package com.paymentsystem.core.domain

import com.paymentsystem.core.domain.enums.MerchantStatus
import com.paymentsystem.core.domain.events.DomainEvent
import com.paymentsystem.core.domain.events.MerchantCreated
import com.paymentsystem.core.domain.valueobjects.EmailAddress
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class Merchant(
    val id: UUID,
    val businessName: String,
    val email: EmailAddress,
    val failedAttempts: Int,
    val settlementAccount: String,
    val balance: BigDecimal,
    val lockedBalance: BigDecimal,
    val status: MerchantStatus,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()
) {
    companion object {
        fun create(
            businessName: String,
            email: String,
            settlementAccount: String,
            balance: BigDecimal
        ): Merchant {
            val now = ZonedDateTime.now()
            val merchantId = UUID.randomUUID()
            val emailAddress = EmailAddress(email)

            val merchant = Merchant(
                id = merchantId,
                businessName = businessName,
                email = emailAddress,
                settlementAccount = settlementAccount,
                balance = balance,
                lockedBalance = BigDecimal.ZERO,
                failedAttempts = 0,
                status = MerchantStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )

            merchant.addDomainEvent(
                MerchantCreated(
                    merchantId = merchantId,
                    businessName = businessName,
                    email = email,
                    status = MerchantStatus.ACTIVE
                )
            )

            return merchant
        }
    }

    fun isActive(): Boolean = status.isActive()

    fun hasExceededFailedAttempts(): Boolean = failedAttempts >= 5

    fun incrementFailedAttempts(): Merchant {
        val newAttempts = failedAttempts + 1
        return if (newAttempts >= 5) {
            suspendSelf().copy(failedAttempts = newAttempts)
        } else {
            copy(failedAttempts = newAttempts, updatedAt = ZonedDateTime.now())
        }
    }

    private fun suspendSelf(): Merchant {
        return copy(status = MerchantStatus.SUSPENDED, updatedAt = ZonedDateTime.now())
    }

    fun resetFailedAttempts(): Merchant =
        copy(failedAttempts = 0, updatedAt = ZonedDateTime.now())

    fun activate(): Merchant {
        require(status == MerchantStatus.SUSPENDED) { "Only suspended merchants can be activated" }
        return copy(status = MerchantStatus.ACTIVE, updatedAt = ZonedDateTime.now())
    }


    fun reserve(amount: BigDecimal): Merchant {
        val available = balance - lockedBalance
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        require(amount <= available) { "Insufficient available balance" }

        return copy(
            lockedBalance = lockedBalance + amount,
            updatedAt = ZonedDateTime.now()
        )
    }

    fun release(amount: BigDecimal): Merchant {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        require(lockedBalance >= amount) { "Cannot release more than locked" }

        return copy(
            lockedBalance = lockedBalance - amount,
            updatedAt = ZonedDateTime.now()
        )
    }

    fun debit(amount: BigDecimal): Merchant {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        require(lockedBalance >= amount) { "Not enough locked funds to debit" }

        return copy(
            balance = balance - amount,
            lockedBalance = lockedBalance - amount,
            updatedAt = ZonedDateTime.now()
        )
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    fun clearDomainEvents(): Merchant {
        domainEvents.clear()
        return this
    }

    private fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}
