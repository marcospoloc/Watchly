package com.marcospolo.watchly.ui.search

import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Show

/** Every state the search screen can be in; exactly one at a time, so no impossible combinations. */
sealed interface SearchUiState {
    /** Nothing typed yet. */
    data object Idle : SearchUiState

    data object Loading : SearchUiState

    data class Success(val shows: List<Show>) : SearchUiState

    data class Empty(val query: String) : SearchUiState

    data class Error(val error: DomainError) : SearchUiState
}
