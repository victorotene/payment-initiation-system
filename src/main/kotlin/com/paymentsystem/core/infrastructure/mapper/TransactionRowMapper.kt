package com.paymentsystem.core.infrastructure.mapper

import com.paymentsystem.core.domain.Transaction
import com.paymentsystem.core.domain.enums.Currency
import com.paymentsystem.core.domain.enums.DebitStatus
import com.paymentsystem.core.domain.enums.TransactionStatus
import com.paymentsystem.core.domain.valueobjects.Money
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.time.ZoneOffset
import java.util.UUID

private val logger = LoggerFactory.getLogger("RowMappers")

fun mapRowToTransaction(rs: ResultSet): Transaction {
    val transaction = Transaction(
        id = UUID.fromString(rs.getString("id")),
        merchantId = UUID.fromString(rs.getString("merchant_id")),
        merchantRef = rs.getString("merchant_ref"),
        internalRef = rs.getString("internal_ref"),
        amount = Money(rs.getBigDecimal("amount"), Currency.fromCode(rs.getString("currency"))),
        fee = Money(rs.getBigDecimal("fee"), Currency.fromCode(rs.getString("currency"))),
        netAmount = Money(rs.getBigDecimal("net_amount"), Currency.fromCode(rs.getString("currency"))),
        retryCount = rs.getInt("retry_count").takeIf { !rs.wasNull() } ?: 0,
        status = TransactionStatus.valueOf(rs.getString("status")),
        idempotencyKey = rs.getString("idempotency_key"),
        customerSimulatedDebitStatus = DebitStatus.valueOf(rs.getString("customer_simulated_debit_status")),
        createdAt = rs.getTimestamp("created_at").toInstant().atZone(ZoneOffset.UTC),
        updatedAt = rs.getTimestamp("updated_at").toInstant().atZone(ZoneOffset.UTC)
    )
    logger.debug("Mapped row to transaction: ID = {}, Status = {}", transaction.id, transaction.status)
    return transaction
}
