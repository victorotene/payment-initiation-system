package com.paymentsystem.core.application.queries

import com.paymentsystem.core.application.dto.TransactionListResult
import com.paymentsystem.core.application.dto.TransactionResult
import com.paymentsystem.core.application.interfaces.Query
import com.paymentsystem.core.domain.enums.TransactionStatus
import java.time.LocalDateTime
import java.util.UUID

data class ListTransactionsQuery(
    val merchantId: UUID,
    val status: TransactionStatus? = null,
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20
) : Query<TransactionListResult>