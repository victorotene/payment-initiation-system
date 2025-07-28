package com.paymentsystem.core.application.queryhandlers

import com.paymentsystem.core.application.dto.TransactionListResult
import com.paymentsystem.core.application.dto.TransactionResult
import com.paymentsystem.core.application.interfaces.QueryHandler
import com.paymentsystem.core.application.queries.ListTransactionsQuery
import com.paymentsystem.core.application.common.PageRequest
import com.paymentsystem.core.domain.exceptions.MerchantNotFoundException
import com.paymentsystem.core.domain.interfaces.repository.MerchantRepository
import com.paymentsystem.core.domain.interfaces.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ListTransactionsQueryHandler(
    private val transactionRepository: TransactionRepository,
    private val merchantRepository: MerchantRepository
) : QueryHandler<ListTransactionsQuery, TransactionListResult> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun handle(query: ListTransactionsQuery): TransactionListResult {
        logger.info("Handling ListTransactionsQuery for merchant: ${query.merchantId}")

        merchantRepository.findById(query.merchantId)
            ?: throw MerchantNotFoundException("Merchant ${query.merchantId} not found")

        val page = maxOf(0, query.page)
        val size = when {
            query.size <= 0 -> 20
            query.size > 100 -> 100
            else -> query.size
        }

        if (query.fromDate != null && query.toDate != null && query.fromDate.isAfter(query.toDate)) {
            throw IllegalArgumentException("fromDate cannot be after toDate")
        }

        val pageRequest = PageRequest(page, size)

        val pageResult = transactionRepository.findByMerchantIdWithFilters(
            merchantId = query.merchantId,
            status = query.status,
            fromDate = query.fromDate,
            toDate = query.toDate,
            pageRequest = pageRequest
        )

        val message = "Transaction fetched successfully"

        val transactionDtos = pageResult.content.map { TransactionResult.Companion.fromTransaction(it, message) }

        logger.info("Retrieved ${transactionDtos.size} transactions for merchant: ${query.merchantId}")

        return TransactionListResult(
            transactions = transactionDtos,
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
            currentPage = pageResult.currentPage,
            pageSize = pageResult.pageSize,
            hasNext = pageResult.hasNext,
            hasPrevious = pageResult.hasPrevious,
        )
    }
}