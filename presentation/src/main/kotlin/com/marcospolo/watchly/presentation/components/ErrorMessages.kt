package com.marcospolo.watchly.presentation.components

import androidx.annotation.StringRes
import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.presentation.R

/** Maps a domain failure to a user-facing, localized explanation. */
@StringRes
fun DomainError.messageRes(): Int = when (this) {
    DomainError.Network -> R.string.error_network
    DomainError.NotFound -> R.string.error_not_found
    DomainError.Server -> R.string.error_server
    DomainError.Unknown -> R.string.error_unknown
}
