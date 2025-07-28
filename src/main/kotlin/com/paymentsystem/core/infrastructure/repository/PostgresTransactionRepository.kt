package com.paymentsystem.core.infrastructure.repository

import com.paymentsystem.core.application.common.PageRequest
import com.paymentsystem.core.application.common.PageResult
import com.paymentsystem.core.domain.Transaction
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.interfaces.repository.TransactionRepository
import com.paymentsystem.core.infrastructure.mapper.mapRowToTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.math.ceil

@Repository
class PostgresTransactionRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) : TransactionRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(PostgresTransactionRepository::class.java)
        private val transactionRowMapper = RowMapper { rs, _ -> mapRowToTransaction(rs) }
    }

    override suspend fun findByIdempotencyKey(idempotencyKey: String): Transaction? {
        logger.info("Searching for transaction by idempotencyKey = {}", idempotencyKey)

        val sql = """
            SELECT * FROM transactions 
            WHERE idempotency_key = :idempotencyKey
        """.trimIndent()

        return executeQuery(sql, MapSqlParameterSource("idempotencyKey", idempotencyKey))
    }

    override suspend fun findById(transactionId: UUID): Transaction? {
        logger.info("🔍 Searching for transaction by ID = {}", transactionId)

        val sql = """
            SELECT * FROM transactions 
            WHERE id = :id
        """.trimIndent()

        return executeQuery(sql, MapSqlParameterSource("id", transactionId))
    }

    override suspend fun save(transaction: Transaction): Transaction {
        logger.info("Saving new transaction: ID = {}, Status = {}, Amount = {}",
            transaction.id, transaction.status, transaction.amount.amount)

        val sql = """
            INSERT INTO transactions (
                id, merchant_id, merchant_ref, internal_ref, amount, currency,
                fee, net_amount, retry_count, status, idempotency_key,
                customer_simulated_debit_status, settlement_batch_id, created_at, updated_at
            ) VALUES (
                :id, :merchantId, :merchantRef, :internalRef, :amount, :currency,
                :fee, :netAmount, :retryCount, :status, :idempotencyKey,
                :customerSimulatedDebitStatus, :settlementBatchId, :createdAt, :updatedAt
            )
        """.trimIndent()

        val params = buildTransactionParams(transaction)
        val rowsInserted = jdbcTemplate.update(sql, params)

        logger.info("Transaction saved: ID = {}, rows affected = {}", transaction.id, rowsInserted)

        return findById(transaction.id) ?: transaction
    }

    @Transactional
    override suspend fun update(transaction: Transaction): Transaction {
        logger.info("Updating transaction: ID = {}, Status = {}, RetryCount = {}",
            transaction.id, transaction.status, transaction.retryCount)

        val sql = """
            UPDATE transactions 
            SET status = :status, 
                customer_simulated_debit_status = :customerSimulatedDebitStatus,
                retry_count = :retryCount,
                settlement_batch_id = :settlementBatchId,
                updated_at = :updatedAt
            WHERE id = :id
        """.trimIndent()

        val params = MapSqlParameterSource().apply {
            addValue("status", transaction.status.name)
            addValue("customerSimulatedDebitStatus", transaction.customerSimulatedDebitStatus.name)
            addValue("retryCount", transaction.retryCount)
            addValue("settlementBatchId", transaction.settlementBatchId)
            addValue("updatedAt", transaction.updatedAt.toOffsetDateTime())
            addValue("id", transaction.id)
        }

        val rowsUpdated = jdbcTemplate.update(sql, params)
        logger.info("Transaction updated: ID = {}, rows affected = {}", transaction.id, rowsUpdated)

        return findById(transaction.id) ?: transaction
    }

    override suspend fun findTransactionsByMerchantAndDateRange(
        merchantId: UUID,
        startDate: ZonedDateTime,
        endDate: ZonedDateTime,
        status: TransactionStatus?,
        limit: Int,
        offset: Int
    ): List<Transaction> {
        logger.info("Searching transactions for merchantId = {}, between {} and {}, status = {}",
            merchantId, startDate, endDate, status)

        val sql = buildString {
            append(
                """
                SELECT * FROM transactions
                WHERE merchant_id = :merchantId
                  AND created_at >= :startDate
                  AND created_at <= :endDate
            """.trimIndent()
            )

            if (status != null) {
                append(" AND status = :status")
            }

            append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
        }

        val params = MapSqlParameterSource().apply {
            addValue("merchantId", merchantId)
            addValue("startDate", startDate.toOffsetDateTime())
            addValue("endDate", endDate.toOffsetDateTime())
            addValue("limit", limit)
            addValue("offset", offset)
            if (status != null) {
                addValue("status", status.name)
            }
        }

        val transactions = jdbcTemplate.query(sql, params, transactionRowMapper)
        logger.info("Found {} transactions for merchant {}, status = {}", transactions.size, merchantId, status)
        return transactions
    }

     override suspend fun findByMerchantIdWithFilters(
        merchantId: UUID,
        status: TransactionStatus?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        pageRequest: PageRequest
    ): PageResult<Transaction> = withContext(Dispatchers.IO) {

        // Build dynamic WHERE clause
        val whereConditions = mutableListOf<String>()
        val parameters = MapSqlParameterSource()

        // Always filter by merchant ID
        whereConditions.add("merchant_id = :merchantId")
        parameters.addValue("merchantId", merchantId)

        // Add status filter if provided
        status?.let {
            whereConditions.add("status = :status")
            parameters.addValue("status", it.name)
        }

        // Add date range filters if provided
        fromDate?.let {
            whereConditions.add("created_at >= :fromDate")
            parameters.addValue("fromDate", it)
        }

        toDate?.let {
            whereConditions.add("created_at <= :toDate")
            parameters.addValue("toDate", it)
        }

        val whereClause = whereConditions.joinToString(" AND ")

        // Count query for pagination
        val countSql = """
            SELECT COUNT(*) 
            FROM transactions 
            WHERE $whereClause
        """.trimIndent()

        val totalElements = namedParameterJdbcTemplate.queryForObject(
            countSql,
            parameters,
            Long::class.java
        ) ?: 0L

        // Calculate pagination
        val totalPages = if (totalElements == 0L) 0 else ceil(totalElements.toDouble() / pageRequest.size).toInt()
        val offset = pageRequest.page * pageRequest.size

        // Data query with pagination
        val dataSql = """
            SELECT 
                id,
                merchant_id,
                merchant_ref,
                internal_ref,
                idempotency_key,
                amount,
                currency,
                fee,
                net_amount,
                retry_count,
                status,
                customer_simulated_debit_status,
                settlement_batch_id,
                created_at,
                updated_at
            FROM transactions 
            WHERE $whereClause
            ORDER BY created_at DESC, id DESC
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        parameters.addValue("limit", pageRequest.size)
        parameters.addValue("offset", offset)

        val transactions = namedParameterJdbcTemplate.query(dataSql, parameters) { rs, _ ->
            mapRowToTransaction(rs)
        }

        PageResult(
            content = transactions,
            totalElements = totalElements,
            totalPages = totalPages,
            currentPage = pageRequest.page,
            pageSize = pageRequest.size
        )
    }


    private suspend fun executeQuery(sql: String, params: MapSqlParameterSource): Transaction? {
        return try {
            jdbcTemplate.queryForObject(sql, params, transactionRowMapper)
                .also { logger.info("Transaction fetched: ID = {}", it?.id) }
        } catch (e: Exception) {
            logger.warn("Query returned no result or failed: {}", e.message)
            null
        }
    }

    private fun buildTransactionParams(transaction: Transaction): MapSqlParameterSource {
        return MapSqlParameterSource().apply {
            addValue("id", transaction.id)
            addValue("merchantId", transaction.merchantId)
            addValue("merchantRef", transaction.merchantRef)
            addValue("internalRef", transaction.internalRef)
            addValue("amount", transaction.amount.amount)
            addValue("currency", transaction.amount.currency.code)
            addValue("fee", transaction.fee.amount)
            addValue("netAmount", transaction.netAmount.amount)
            addValue("retryCount", transaction.retryCount)
            addValue("status", transaction.status.name)
            addValue("idempotencyKey", transaction.idempotencyKey)
            addValue("customerSimulatedDebitStatus", transaction.customerSimulatedDebitStatus.name)
            addValue("settlementBatchId", transaction.settlementBatchId)
            addValue("createdAt", transaction.createdAt.toOffsetDateTime())
            addValue("updatedAt", transaction.updatedAt.toOffsetDateTime())
        }
    }
}
