package com.example.casefilesmobile

import android.widget.DatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

suspend fun <T> withAsync(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    return withContext(context) {
        async { block() }
    }
}




