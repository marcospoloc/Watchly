package com.marcospolo.watchly.domain.model

/** Failures the presentation layer knows how to explain to the user. */
sealed interface DomainError {
    /** No connectivity or the request timed out. */
    data object Network : DomainError

    /** The requested resource does not exist. */
    data object NotFound : DomainError

    /** The server answered with an unexpected error status. */
    data object Server : DomainError

    /** Anything else, such as a malformed response. */
    data object Unknown : DomainError
}
