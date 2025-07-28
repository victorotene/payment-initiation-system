package com.paymentsystem.core.application.interfaces

interface Command<R>

interface CommandHandler<C : Command<R>, R> {
    suspend fun handle(command: C): R
}
