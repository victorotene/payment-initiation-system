package com.paymentsystem.core.infrastructure.mapper

import com.paymentsystem.core.domain.entities.SettlementBatch
import com.paymentsystem.core.domain.enums.Currency
import com.paymentsystem.core.domain.enums.SettlementStatus
import com.paymentsystem.core.domain.valueobjects.Money
import java.sql.ResultSet
import java.time.ZoneOffset
import java.util.UUID

fun mapRowToSettlementBatch(rs: ResultSet): SettlementBatch {
    val currency = Currency.fromCode(rs.getString("currency"))
    return SettlementBatch(
        id = UUID.fromString(rs.getString("id")),
        batchRef = rs.getString("batch_ref"),
        merchantId = UUID.fromString(rs.getString("merchant_id")),
        totalAmount = Money(rs.getBigDecimal("total_amount"), currency),
        totalFee = Money(rs.getBigDecimal("total_fee"), currency),
        netAmount = Money(rs.getBigDecimal("net_amount"), currency),
        transactionCount = rs.getInt("transaction_count"),
        status = SettlementStatus.valueOf(rs.getString("status")),
        createdAt = rs.getTimestamp("created_at").toInstant().atZone(ZoneOffset.UTC),
        updatedAt = rs.getTimestamp("updated_at").toInstant().atZone(ZoneOffset.UTC)
    )
}
