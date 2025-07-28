package com.paymentsystem.core.application.interfaces


interface QueryBus {
    suspend fun <TResult> send(query: Query<TResult>): TResult
}