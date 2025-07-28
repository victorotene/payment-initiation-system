package com.paymentsystem.core.application.interfaces

import com.paymentsystem.core.domain.events.DomainEvent

interface DomainEventDispatcher {
    suspend fun dispatch(event: DomainEvent)
}