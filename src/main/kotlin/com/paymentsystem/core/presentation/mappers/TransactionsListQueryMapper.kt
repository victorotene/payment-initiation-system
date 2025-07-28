package com.paymentsystem.core.presentation.mappers

import com.paymentsystem.core.application.queries.ListTransactionsQuery
import com.paymentsystem.core.domain.enums.TransactionStatus
import java.time.LocalDateTime
import java.util.UUID

object TransactionsListQueryMapper{
fun toQuery(
    merchantId: UUID,
    status: TransactionStatus?,
    fromDate: LocalDateTime?,
    toDate: LocalDateTime?,
    page: Int,
    size: Int
): ListTransactionsQuery = ListTransactionsQuery(
    merchantId = merchantId,
    status = status,
    fromDate = fromDate,
    toDate = toDate,
    page = page,
    size = size
)
}