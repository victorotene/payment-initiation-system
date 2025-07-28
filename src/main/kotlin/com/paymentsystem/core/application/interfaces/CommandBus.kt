package com.paymentsystem.core.application.interfaces

interface CommandBus {
    suspend fun <R> send(command: Command<R>): R
}
