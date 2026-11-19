package com.marcospolo.watchly.data.remote

import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Outcome
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

private const val HTTP_NOT_FOUND = 404

/**
 * Runs a network call and maps any failure to a [DomainError].
 *
 * [CancellationException] is rethrown on purpose: swallowing it would turn a
 * cancelled search (the user typed a newer query) into a fake error and break
 * structured concurrency.
 */
internal suspend fun <T> safeApiCall(call: suspend () -> T): Outcome<T> = try {
    Outcome.Success(call())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
    Outcome.Failure(error.toDomainError())
}

internal fun Throwable.toDomainError(): DomainError = when (this) {
    is IOException -> DomainError.Network
    is HttpException -> if (code() == HTTP_NOT_FOUND) DomainError.NotFound else DomainError.Server
    else -> DomainError.Unknown
}
