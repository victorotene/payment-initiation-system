import com.paymentsystem.core.application.dto.TransactionListResult
import com.paymentsystem.core.presentation.response.PaginationResponse
import com.paymentsystem.core.presentation.response.TransactionListResponse
import com.paymentsystem.core.presentation.response.TransactionResponse
import java.time.format.DateTimeFormatter

object TransactionListResponseMapper {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun fromResult(result: TransactionListResult): TransactionListResponse {
        return TransactionListResponse(
            transactions = result.transactions.map {
                TransactionResponse(
                    id = it.id,
                    merchantRef = it.merchantRef,
                    amount = it.amount.toPlainString(),
                    currency = it.currency,
                    fee = it.fee.toPlainString(),
                    netAmount = it.netAmount.toPlainString(),
                    status = it.status.name,
                    customerSimulatedDebitStatus = it.customerSimulatedDebitStatus.name,
                    retryCount = it.retryCount,
                    settlementBatchId = it.settlementBatchId,
                    createdAt = it.createdAt.toLocalDateTime().format(dateFormatter),
                    updatedAt = it.updatedAt.toLocalDateTime().format(dateFormatter)
                )
            },
            pagination = PaginationResponse(
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                currentPage = result.currentPage,
                pageSize = result.pageSize,
                hasNext = result.hasNext,
                hasPrevious = result.hasPrevious
            )
        )
    }
}
