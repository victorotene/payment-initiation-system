package com.paymentsystem.core.infrastructure.repository

import com.paymentsystem.core.domain.Transaction
import com.paymentsystem.core.domain.entities.SettlementBatch
import com.paymentsystem.core.domain.enums.Currency
import com.paymentsystem.core.domain.enums.SettlementStatus
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.interfaces.repository.SettlementRepository
import com.paymentsystem.core.domain.valueobjects.Money
import com.paymentsystem.core.infrastructure.mapper.mapRowToSettlementBatch
import com.paymentsystem.core.infrastructure.mapper.mapRowToTransaction
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.ZoneId
import java.util.UUID

@Repository
class PostgresSettlementRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : SettlementRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(PostgresSettlementRepository::class.java)
        private val transactionRowMapper = RowMapper { rs, _ -> mapRowToTransaction(rs) }
        private val settlementBatchRowMapper = RowMapper { rs, _ -> mapRowToSettlementBatch(rs) }
    }

    override suspend fun findSettlableTransactions(
        merchantId: UUID?,
        limit: Int
    ): List<Transaction> {
        logger.debug("Finding settlable transactions: merchantId={}, limit={}", merchantId, limit)

        val sql = if (merchantId != null) {
            """
            SELECT * FROM transactions
            WHERE merchant_id = :merchantId
              AND status = :successStatus
              AND settlement_batch_id IS NULL
            ORDER BY created_at ASC
            LIMIT :limit
            """.trimIndent()
        } else {
            """
            SELECT * FROM transactions
            WHERE status = :successStatus
              AND settlement_batch_id IS NULL
            ORDER BY merchant_id, created_at ASC
            LIMIT :limit
            """.trimIndent()
        }

        val params = MapSqlParameterSource().apply {
            addValue("successStatus", TransactionStatus.SUCCESS.name)
            addValue("limit", limit)
            merchantId?.let { addValue("merchantId", it) }
        }

        return jdbcTemplate.query(sql, params, transactionRowMapper)
            .also { logger.debug("Found {} settlable transactions", it.size) }
    }

    @Transactional
    override suspend fun saveSettlementBatch(batch: SettlementBatch): SettlementBatch {
        logger.debug("Saving settlement batch: {}", batch.id)

        val sql = """
            INSERT INTO settlement_batches (
                id, batch_ref, merchant_id, total_amount, total_fee, net_amount,
                currency, transaction_count, status, created_at, updated_at
            )
            VALUES (
                :id, :batchRef, :merchantId, :totalAmount, :totalFee, :netAmount,
                :currency, :transactionCount, :status, :createdAt, :updatedAt
            )
            ON CONFLICT (id) DO UPDATE SET
                status = EXCLUDED.status,
                updated_at = EXCLUDED.updated_at
        """.trimIndent()

        val params = buildSettlementBatchParams(batch)
        jdbcTemplate.update(sql, params)

        return findSettlementBatchById(batch.id) ?: batch
    }

    @Transactional
    override suspend fun updateTransactionsWithBatch(
        transactionIds: List<UUID>,
        batchId: UUID
    ): Int {
        logger.debug("Updating {} transactions with batch {}", transactionIds.size, batchId)
        if (transactionIds.isEmpty()) return 0

        val sql = """
            UPDATE transactions
            SET settlement_batch_id = :batchId,
                status = :settledStatus,
                updated_at = now() at time zone 'UTC'
            WHERE id = ANY(:transactionIds)
              AND status = :successStatus
              AND settlement_batch_id IS NULL
        """.trimIndent()

        val params = MapSqlParameterSource().apply {
            addValue("batchId", batchId)
            addValue("settledStatus", TransactionStatus.SETTLED.name)
            addValue("successStatus", TransactionStatus.SUCCESS.name)
            addValue("transactionIds", transactionIds.toTypedArray())
        }

        val updated = jdbcTemplate.update(sql, params)
        logger.debug("Updated {} transactions with batch {}", updated, batchId)
        return updated
    }

    override suspend fun findSettlementBatchById(batchId: UUID): SettlementBatch? {
        logger.debug("Finding settlement batch by ID: {}", batchId)
        val sql = """
            SELECT * FROM settlement_batches
            WHERE id = :batchId
        """.trimIndent()

        return try {
            jdbcTemplate.queryForObject(
                sql,
                MapSqlParameterSource("batchId", batchId),
                settlementBatchRowMapper
            )
        } catch (e: Exception) {
            logger.debug("Settlement batch not found: {}", batchId)
            null
        }
    }

    override suspend fun findSettlementBatchesByMerchant(
        merchantId: UUID,
        limit: Int,
        offset: Int
    ): List<SettlementBatch> {
        logger.debug("Finding settlement batches for merchant: {}", merchantId)
        val sql = """
            SELECT * FROM settlement_batches
            WHERE merchant_id = :merchantId
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        val params = MapSqlParameterSource().apply {
            addValue("merchantId", merchantId)
            addValue("limit", limit)
            addValue("offset", offset)
        }

        return jdbcTemplate.query(sql, params, settlementBatchRowMapper)
    }

    private fun buildSettlementBatchParams(batch: SettlementBatch): MapSqlParameterSource =
        MapSqlParameterSource().apply {
            addValue("id", batch.id)
            addValue("batchRef", batch.batchRef)
            addValue("merchantId", batch.merchantId)
            addValue("totalAmount", batch.totalAmount.amount)
            addValue("totalFee", batch.totalFee.amount)
            addValue("netAmount", batch.netAmount.amount)
            addValue("currency", batch.totalAmount.currency.code)
            addValue("transactionCount", batch.transactionCount)
            addValue("status", batch.status.name)
            addValue("createdAt", batch.createdAt.toOffsetDateTime())
            addValue("updatedAt", batch.updatedAt.toOffsetDateTime())
        }
}
