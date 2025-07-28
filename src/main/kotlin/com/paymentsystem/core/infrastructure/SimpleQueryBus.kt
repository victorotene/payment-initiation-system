package com.paymentsystem.core.infrastructure

import com.paymentsystem.core.application.interfaces.Query
import com.paymentsystem.core.application.interfaces.QueryBus
import com.paymentsystem.core.application.interfaces.QueryHandler
import org.slf4j.LoggerFactory
import org.springframework.core.ResolvableType
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class SimpleQueryBus(
    handlers: List<QueryHandler<*, *>>
) : QueryBus {

    private val logger = LoggerFactory.getLogger(SimpleQueryBus::class.java)

    private val handlerMap: Map<KClass<*>, QueryHandler<*, *>> = handlers.associateBy { handler ->
        val resolvableType = ResolvableType.forClass(QueryHandler::class.java, handler.javaClass)
        val queryClass = resolvableType.getGeneric(0).resolve()
            ?: throw IllegalStateException("Unable to resolve query type for handler: ${handler.javaClass}")
        queryClass.kotlin
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <R> send(query: Query<R>): R {
        val handler = handlerMap[query::class]
            ?: throw IllegalArgumentException(
                "No handler found for query: ${query::class.simpleName}. " +
                        "Available handlers: ${handlerMap.keys.joinToString { it.simpleName ?: "Unknown" }}"
            )

        logger.info("Dispatching query: ${query::class.simpleName} to ${handler::class.simpleName}")
        return (handler as QueryHandler<Query<R>, R>).handle(query)
    }
}
