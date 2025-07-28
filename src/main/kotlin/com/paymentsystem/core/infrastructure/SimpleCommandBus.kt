package com.paymentsystem.core.infrastructure

import com.paymentsystem.core.application.interfaces.Command
import com.paymentsystem.core.application.interfaces.CommandBus
import com.paymentsystem.core.application.interfaces.CommandHandler
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.core.ResolvableType
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class SimpleCommandBus(
    handlers: List<CommandHandler<*, *>>
) : CommandBus {

    private val logger = LoggerFactory.getLogger(SimpleCommandBus::class.java)

    private val handlerMap: Map<KClass<*>, CommandHandler<*, *>> = handlers.associateBy { handler ->
        val resolvableType = ResolvableType.forClass(CommandHandler::class.java, handler.javaClass)
        val commandClass = resolvableType.getGeneric(0).resolve()
            ?: throw IllegalStateException("Unable to resolve command type for handler: ${handler.javaClass}")
        commandClass.kotlin
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <R> send(command: Command<R>): R {
        val handler = handlerMap[command::class]
            ?: throw IllegalArgumentException(
                "No handler found for command: ${command::class.simpleName}. " +
                        "Available handlers: ${handlerMap.keys.joinToString { it.simpleName ?: "Unknown" }}"
            )

        logger.info("Dispatching command: ${command::class.simpleName} to ${handler::class.simpleName}")
        return (handler as CommandHandler<Command<R>, R>).handle(command)
    }
}
