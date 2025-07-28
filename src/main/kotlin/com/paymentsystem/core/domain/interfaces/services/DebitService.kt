package com.paymentsystem.core.domain.interfaces.services

import com.paymentsystem.core.domain.enums.DebitStatus
import java.math.BigDecimal
import java.util.UUID
import javax.print.attribute.standard.Destination

interface DebitService {
    suspend fun processDebit(
        reference: String,
        transactionId: String,
        amount: BigDecimal,
        currency: String,
        merchantRef: String,
        destination: String,
        status: String
    )
}