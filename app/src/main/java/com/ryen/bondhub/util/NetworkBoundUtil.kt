package com.ryen.bondhub.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// This should be in a utility class
fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<ResultType>,
    fetch: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType) -> Boolean = { true },
    onFetchFailed: (Throwable) -> Unit = { },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Flow<ResultType> = flow {
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(data) // Emit cached data first

        try {
            withContext(dispatcher) {
                val fetchedResult = fetch()
                saveFetchResult(fetchedResult)
            }
            query().map { newData ->
                newData
            }
        } catch (throwable: Throwable) {
            onFetchFailed(throwable)
            query().map { cachedData ->
                cachedData
            }
        }
    } else {
        query().map { cachedData ->
            cachedData
        }
    }

    emitAll(flow)
}