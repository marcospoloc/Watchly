package com.marcospolo.watchly.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.usecase.SearchShowsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Wait for the user to pause typing before searching; also keeps us under TVMaze's rate limit. */
internal val SEARCH_DEBOUNCE = 300.milliseconds

/** Key of the search text in [SavedStateHandle], so it survives process death. */
private const val QUERY_KEY = "query"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchShows: SearchShowsUseCase
) : ViewModel() {
    /**
     * Raw text of the search field, updated on every keystroke. It lives in the
     * [SavedStateHandle] so the search is restored if the system kills the process.
     */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")

    /** Incremented by "Retry" so the same query runs again even though it did not change. */
    private val retryRequests = MutableStateFlow(0)

    /**
     * `flatMapLatest` is what implements cancellation: every change of the query
     * cancels the coroutine of the previous search, whether it is still waiting
     * for the debounce or running its HTTP call, so results of an outdated query
     * can never reach the UI.
     *
     * The debounce lives inside [searchStates] on purpose. With `debounce()`
     * before `flatMapLatest`, the previous search would keep running during the
     * debounce window and could show its results next to the new query.
     *
     * Shared `Lazily` rather than `WhileSubscribed`: the search screen stops
     * collecting while a detail is open, and restarting the upstream on return
     * would run the search again, flash the skeleton and reset the list scroll.
     *
     * A query restored after process death starts as `Loading`, so the idle
     * message does not flash before the restored search runs again.
     */
    val uiState: StateFlow<SearchUiState> = query
        .map { it.trim() }
        .distinctUntilChanged()
        .combine(retryRequests) { query, _ -> query }
        .flatMapLatest(::searchStates)
        .stateIn(viewModelScope, SharingStarted.Lazily, initialState(query.value))

    fun onQueryChange(newQuery: String) {
        savedStateHandle[QUERY_KEY] = newQuery
    }

    fun onClearQuery() {
        savedStateHandle[QUERY_KEY] = ""
    }

    fun onRetry() {
        retryRequests.value++
    }

    private fun initialState(query: String): SearchUiState =
        if (query.isBlank()) SearchUiState.Idle else SearchUiState.Loading

    private fun searchStates(query: String): Flow<SearchUiState> {
        if (query.isEmpty()) return flowOf(SearchUiState.Idle)
        return flow {
            // Debounce: cancelled by flatMapLatest if the user keeps typing.
            delay(SEARCH_DEBOUNCE)
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
