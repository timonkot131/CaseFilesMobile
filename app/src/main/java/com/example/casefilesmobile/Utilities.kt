package com.example.casefilesmobile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun <T> withAsync(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    return withContext(context) {
        async { block() }
    }
}


