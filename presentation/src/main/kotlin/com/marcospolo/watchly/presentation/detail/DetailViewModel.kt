package com.marcospolo.watchly.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.usecase.GetShowDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Must match the property name of `DetailDestination.showId`, which Navigation uses as the argument key. */
internal const val SHOW_ID_ARG = "showId"

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getShowDetail: GetShowDetailUseCase
) : ViewModel() {
    private val showId: Int = checkNotNull(savedStateHandle[SHOW_ID_ARG]) { "Missing $SHOW_ID_ARG argument" }

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        // A retry tapped twice must not run two requests in parallel.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            _uiState.value = when (val result = getShowDetail(showId)) {
                is Outcome.Success -> DetailUiState.Success(result.value)
                is Outcome.Failure -> DetailUiState.Error(result.error)
            }
        }
    }
}
