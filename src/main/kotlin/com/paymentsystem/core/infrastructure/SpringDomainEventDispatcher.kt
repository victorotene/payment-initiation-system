package com.paymentsystem.core.infrastructure

import com.paymentsystem.core.application.interfaces.DomainEventDispatcher
import com.paymentsystem.core.domain.events.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventDispatcher(
    private val eventPublisher: ApplicationEventPublisher
) : DomainEventDispatcher {

    private val logger = LoggerFactory.getLogger(SpringDomainEventDispatcher::class.java)

    override suspend fun dispatch(event: DomainEvent) {
        logger.debug("Dispatching domain event: {} with ID: {}", event::class.simpleName, event.eventId)
        eventPublisher.publishEvent(event)
        logger.debug("Domain event dispatched successfully")
    }
}