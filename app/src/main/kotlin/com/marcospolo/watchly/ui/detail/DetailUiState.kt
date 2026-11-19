package com.marcospolo.watchly.ui.detail

import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.ShowDetail

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Success(val show: ShowDetail) : DetailUiState

    data class Error(val error: DomainError) : DetailUiState
}
