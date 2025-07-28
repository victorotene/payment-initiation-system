package com.paymentsystem.core.application.common

data class PageResult<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
) {
    val hasNext: Boolean = currentPage < totalPages - 1
    val hasPrevious: Boolean = currentPage > 0
}