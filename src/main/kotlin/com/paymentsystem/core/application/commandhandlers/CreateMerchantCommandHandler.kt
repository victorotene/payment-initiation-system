package com.paymentsystem.core.application.commandhandlers

import com.paymentsystem.core.application.commands.CreateMerchantCommand
import com.paymentsystem.core.application.dto.CreateMerchantResult
import com.paymentsystem.core.application.interfaces.CommandHandler
import com.paymentsystem.core.application.interfaces.DomainEventDispatcher
import com.paymentsystem.core.domain.Merchant
import com.paymentsystem.core.domain.exceptions.MerchantAlreadyExistsException
import com.paymentsystem.core.domain.interfaces.repository.MerchantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CreateMerchantCommandHandler(
    private val merchantRepository: MerchantRepository,
    private val eventDispatcher: DomainEventDispatcher
) : CommandHandler<CreateMerchantCommand, CreateMerchantResult> {

    private val logger = LoggerFactory.getLogger(CreateMerchantCommandHandler::class.java)

    override suspend fun handle(command: CreateMerchantCommand): CreateMerchantResult {
        logger.info("Handling CreateMerchantCommand for email: {}", command.email)

        if (merchantRepository.existsByEmail(command.email)) {
            throw MerchantAlreadyExistsException("Merchant with email ${command.email} already exists")
        }

        val merchant = Merchant.create(
            businessName = command.businessName,
            email = command.email,
            settlementAccount = command.settlementAccount,
            balance = command.balance
        )

        logger.debug("Created merchant domain object with ID: {}", merchant.id)

        val savedMerchant = merchantRepository.save(merchant)

        logger.info("Successfully saved merchant with ID: {}", savedMerchant.id)

        savedMerchant.getDomainEvents().forEach { event ->
            logger.debug("Dispatching domain event: {}", event::class.simpleName)
            eventDispatcher.dispatch(event)
        }

        return CreateMerchantResult.from(savedMerchant.clearDomainEvents())
    }
}
