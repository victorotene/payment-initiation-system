package com.paymentsystem.core.infrastructure.repository

import com.paymentsystem.core.domain.Merchant
import com.paymentsystem.core.domain.enums.MerchantStatus
import com.paymentsystem.core.domain.interfaces.repository.MerchantRepository
import com.paymentsystem.core.domain.valueobjects.EmailAddress
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.ZoneId
import java.util.UUID

@Repository
class PostgresMerchantRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : MerchantRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(PostgresMerchantRepository::class.java)
    }

    @Transactional
    override suspend fun save(merchant: Merchant): Merchant {
        logger.debug("Saving merchant with ID: {}", merchant.id)

        val sql = """
            INSERT INTO merchants (
                id, business_name, email, settlement_account, status, 
                balance, locked_balance, failed_attempts
            )
            VALUES (
                :id, :businessName, :email, :settlementAccount, :status, 
                :balance, :lockedBalance, :failedAttempts
            )
            ON CONFLICT (id) DO UPDATE SET
                business_name = EXCLUDED.business_name,
                email = EXCLUDED.email,
                settlement_account = EXCLUDED.settlement_account,
                status = EXCLUDED.status,
                balance = EXCLUDED.balance,
                locked_balance = EXCLUDED.locked_balance,
                failed_attempts = EXCLUDED.failed_attempts
        """.trimIndent()

        val params = MapSqlParameterSource().apply {
            addValue("id", merchant.id)
            addValue("businessName", merchant.businessName)
            addValue("email", merchant.email.value)
            addValue("settlementAccount", merchant.settlementAccount)
            addValue("status", merchant.status.name)
            addValue("balance", merchant.balance)
            addValue("lockedBalance", merchant.lockedBalance)
            addValue("failedAttempts", merchant.failedAttempts)
        }

        jdbcTemplate.update(sql, params)
        logger.debug("Merchant saved successfully: {}", merchant.id)

        // Return merchant with updated timestamps from database
        return findById(merchant.id) ?: merchant
    }

    override suspend fun findById(id: UUID): Merchant? {
        logger.debug("Finding merchant by ID: {}", id)

        val sql = """
            SELECT id, business_name, email, settlement_account, status, 
                   balance, locked_balance, failed_attempts, created_at, updated_at
            FROM merchants
            WHERE id = :id
        """.trimIndent()

        return executeQuery(sql, MapSqlParameterSource("id", id))
    }

    override suspend fun findByEmail(email: String): Merchant? {
        logger.debug("Finding merchant by email: {}", email)

        val sql = """
            SELECT id, business_name, email, settlement_account, status, 
                   balance, locked_balance, failed_attempts, created_at, updated_at
            FROM merchants
            WHERE email = :email
        """.trimIndent()

        return executeQuery(sql, MapSqlParameterSource("email", email))
    }

    override suspend fun existsByEmail(email: String): Boolean {
        logger.debug("Checking if merchant exists by email: {}", email)

        val sql = "SELECT 1 FROM merchants WHERE email = :email LIMIT 1"
        val params = MapSqlParameterSource("email", email)

        return try {
            jdbcTemplate.queryForObject(sql, params, Int::class.java) != null
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun executeQuery(sql: String, params: MapSqlParameterSource): Merchant? {
        return try {
            jdbcTemplate.queryForObject(sql, params) { rs, _ -> mapRowToMerchant(rs) }
        } catch (e: Exception) {
            logger.debug("Query execution returned no results")
            null
        }
    }

    private fun buildMerchantParams(merchant: Merchant): MapSqlParameterSource {
        return MapSqlParameterSource().apply {
            addValue("id", merchant.id)
            addValue("businessName", merchant.businessName)
            addValue("email", merchant.email.value)
            addValue("settlementAccount", merchant.settlementAccount)
            addValue("status", merchant.status.name)
            addValue("balance", merchant.balance)
            addValue("lockedBalance", merchant.lockedBalance)
            addValue("failedAttempts", merchant.failedAttempts)
            addValue("createdAt", merchant.createdAt.toOffsetDateTime())
            addValue("updatedAt", merchant.updatedAt.toOffsetDateTime())
        }
    }
    private fun mapRowToMerchant(rs: ResultSet): Merchant {
        return Merchant(
            id = UUID.fromString(rs.getString("id")),
            businessName = rs.getString("business_name"),
            email = EmailAddress(rs.getString("email")),
            settlementAccount = rs.getString("settlement_account"),
            balance = rs.getBigDecimal("balance"),
            lockedBalance = rs.getBigDecimal("locked_balance"),
            failedAttempts = rs.getInt("failed_attempts"),
            status = MerchantStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toInstant().atZone(ZoneId.of("UTC")),
            updatedAt = rs.getTimestamp("updated_at").toInstant().atZone(ZoneId.of("UTC")),
        )
    }
}