package com.paymentsystem.core.application.interfaces

interface Query<TResult>

interface QueryHandler<TQuery : Query<TResult>, TResult> {
    suspend fun handle(query: TQuery): TResult
}