package com.paymentsystem.core.application.eventhandler

import com.paymentsystem.core.domain.events.MerchantCreated
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MerchantCreatedEventHandler {
    private val logger = LoggerFactory.getLogger(MerchantCreatedEventHandler::class.java)

    @EventListener
    fun handle(event: MerchantCreated) {
        logger.info("Processing MerchantCreated event for merchant ID: {}", event.merchantId)

        println("Sending welcome email to merchant: ${event.email}")
        println("Merchant ${event.businessName} (ID: ${event.merchantId}) has been created with status: ${event.status}")

        logger.info("Welcome email notification queued for merchant: {}", event.email)
    }
}