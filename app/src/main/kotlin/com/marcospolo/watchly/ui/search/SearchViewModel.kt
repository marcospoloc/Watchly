package com.marcospolo.watchly.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.usecase.SearchShowsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Wait for the user to pause typing before searching; also keeps us under TVMaze's rate limit. */
internal val SEARCH_DEBOUNCE = 300.milliseconds
private const val STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(private val searchShows: SearchShowsUseCase) : ViewModel() {
    private val _query = MutableStateFlow("")

    /** Raw text of the search field, updated on every keystroke. */
    val query: StateFlow<String> = _query.asStateFlow()

    /** Incremented by "Retry" so the same query runs again even though it did not change. */
    private val retryRequests = MutableStateFlow(0)

    /**
     * `flatMapLatest` is what implements cancellation: when a new query arrives,
     * the coroutine running the previous search (and its in-flight HTTP call)
     * is cancelled, so results of an outdated query can never reach the UI.
     */
    val uiState: StateFlow<SearchUiState> = _query
        .debounce(SEARCH_DEBOUNCE)
        .map { it.trim() }
        .distinctUntilChanged()
        .combine(retryRequests) { query, _ -> query }
        .flatMapLatest(::searchStates)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SearchUiState.Idle)

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onClearQuery() {
        _query.value = ""
    }

    fun onRetry() {
        retryRequests.value++
    }

    private fun searchStates(query: String): Flow<SearchUiState> {
        if (query.isEmpty()) return flowOf(SearchUiState.Idle)
        return flow {
            emit(SearchUiState.Loading)
            val state = when (val result = searchShows(query)) {
                is Outcome.Success ->
                    if (result.value.isEmpty()) SearchUiState.Empty(query) else SearchUiState.Success(result.value)

                is Outcome.Failure -> SearchUiState.Error(result.error)
            }
            emit(state)
        }
    }
}
