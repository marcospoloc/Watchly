package com.marcospolo.watchly.domain.model

/**
 * Result of an operation that can fail with a [DomainError].
 *
 * Used instead of throwing so errors are explicit in signatures and never leak
 * infrastructure exceptions (Retrofit, IO) across layers.
 */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>

    data class Failure(val error: DomainError) : Outcome<Nothing>
}

inline fun <T> Outcome<T>.onSuccess(action: (T) -> Unit): Outcome<T> {
    if (this is Outcome.Success) action(value)
    return this
}
