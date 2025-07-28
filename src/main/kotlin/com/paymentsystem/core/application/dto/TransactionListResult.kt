package com.paymentsystem.core.application.dto

data class TransactionListResult(
    val transactions: List<TransactionResult>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)