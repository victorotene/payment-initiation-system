package com.paymentsystem.core.presentation // Ensure this is the correct package for your GlobalExceptionHandler

import com.paymentsystem.core.domain.exceptions.*
import com.paymentsystem.core.presentation.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException // Import this!
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.ZonedDateTime
import java.time.ZoneOffset // Import this!

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // --- Add/Update this handler for validation exceptions ---
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("Validation failed for request: {}", ex.message)

        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }

        val errorResponse = ErrorResponse(
            message = "Validation failed: $errors",
            code = "VALIDATION_ERROR",
            timestamp = ZonedDateTime.now(ZoneOffset.UTC)
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MerchantAlreadyExistsException::class)
    fun handleMerchantAlreadyExists(e: MerchantAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn("Merchant already exists: {}", e.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(e.message ?: "Merchant already exists", "MERCHANT_ALREADY_EXISTS", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(MerchantNotFoundException::class)
    fun handleMerchantNotFound(e: MerchantNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Merchant not found: {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(e.message ?: "Merchant not found", "MERCHANT_NOT_FOUND", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(MerchantInactiveException::class)
    fun handleMerchantInactive(e: MerchantInactiveException): ResponseEntity<ErrorResponse> {
        logger.warn("Merchant is inactive: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(e.message ?: "Merchant is inactive", "MERCHANT_INACTIVE", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(AccountSuspendedException::class)
    fun handleAccountSuspended(e: AccountSuspendedException): ResponseEntity<ErrorResponse> {
        logger.warn("Account suspended: {}", e.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(e.message ?: "Account suspended", "ACCOUNT_SUSPENDED", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(TransactionAlreadyExistsException::class)
    fun handleDuplicateTransaction(e: TransactionAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn("Duplicate transaction: {}", e.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(e.message ?: "Duplicate transaction", "DUPLICATE_TRANSACTION", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(NoUnsettledTransactionsException::class)
    fun handleNoUnsettledTransactions(e: NoUnsettledTransactionsException): ResponseEntity<ErrorResponse> {
        logger.warn("No unsettled transactions: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(e.message ?: "No unsettled transactions to settle", "NO_UNSETTLED_TRANSACTIONS", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInsufficientFunds(e: InsufficientFundsException): ResponseEntity<ErrorResponse> {
        logger.warn("Insufficient funds: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(e.message ?: "Insufficient balance", "INSUFFICIENT_BALANCE", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid input: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(e.message ?: "Invalid input", "INVALID_INPUT", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("Internal server error", "INTERNAL_ERROR", ZonedDateTime.now(ZoneOffset.UTC))) // Add timestamp
    }
}