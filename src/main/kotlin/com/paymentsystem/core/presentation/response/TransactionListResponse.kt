package com.paymentsystem.core.presentation.response

import java.util.UUID

data class TransactionListResponse(
    val transactions: List<TransactionResponse>,
    val pagination: PaginationResponse
)

data class TransactionResponse(
    val id: UUID,
    val merchantRef: String,
    val amount: String,
    val currency: String,
    val fee: String,
    val netAmount: String,
    val status: String,
    val customerSimulatedDebitStatus: String,
    val retryCount: Int,
    val settlementBatchId: UUID?,
    val createdAt: String,
    val updatedAt: String
)

data class PaginationResponse(
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)