package com.paymentsystem.core.domain.events

import com.paymentsystem.core.domain.enums.MerchantStatus
import java.util.UUID

data class MerchantCreated(
    val merchantId: UUID,
    val businessName: String,
    val email: String,
    val status: MerchantStatus
) : DomainEvent()