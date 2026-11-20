package com.marcospolo.watchly.presentation.search

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.usecase.SearchShowsUseCase
import com.marcospolo.watchly.presentation.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchShows: SearchShowsUseCase = mockk()
    private val savedStateHandle = SavedStateHandle()
    private val viewModel by lazy { SearchViewModel(savedStateHandle, searchShows) }

    private val lost = Show(1, "Lost", null, 2004, listOf("Drama"), 8.3)
    private val house = Show(2, "House", null, 2004, listOf("Drama", "Medical"), 8.6)

    @Test
    fun `given no query when observed then the state is idle and nothing is searched`() = runTest {
        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            advanceTimeBy(SEARCH_DEBOUNCE * 2)
            expectNoEvents()
        }
        coVerify(exactly = 0) { searchShows(any()) }
    }

    @Test
    fun `given fast typing when the user pauses then searches only the last query`() = runTest {
        coEvery { searchShows(any()) } returns Outcome.Success(listOf(lost))

        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            viewModel.onQueryChange("l")
            viewModel.onQueryChange("lo")
            viewModel.onQueryChange("lost")

            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Success(listOf(lost)), awaitItem())
        }
        coVerify(exactly = 1) { searchShows(any()) }
        coVerify(exactly = 1) { searchShows("lost") }
    }

    @Test
    fun `given a search in progress when a new query arrives then the old search is cancelled and never shown`() =
        runTest {
            var lostSearchCancelled = false
            coEvery { searchShows("lost") } coAnswers {
                try {
                    delay(SLOW_NETWORK_MILLIS)
                } catch (cancellation: CancellationException) {
                    lostSearchCancelled = true
                    throw cancellation
                }
                Outcome.Success(listOf(lost))
            }
            coEvery { searchShows("house") } returns Outcome.Success(listOf(house))

            viewModel.uiState.test {
                assertEquals(SearchUiState.Idle, awaitItem())

                viewModel.onQueryChange("lost")
                assertEquals(SearchUiState.Loading, awaitItem())

                viewModel.onQueryChange("house")
                assertEquals(SearchUiState.Success(listOf(house)), awaitItem())

                // Even after the slow "lost" response would have arrived, nothing else is emitted.
                advanceTimeBy(SLOW_NETWORK_MILLIS * 2)
                expectNoEvents()
            }
            assertTrue("The outdated search must be cancelled", lostSearchCancelled)
        }

    @Test
    fun `given a search in progress when the query changes then its results are never shown`() = runTest {
        var lostSearchCancelled = false
        coEvery { searchShows("lost") } coAnswers {
            try {
                // Answers before the debounce of the next query would end.
                delay(FAST_NETWORK_MILLIS)
            } catch (cancellation: CancellationException) {
                lostSearchCancelled = true
                throw cancellation
            }
            Outcome.Success(listOf(lost))
        }
        coEvery { searchShows("house") } returns Outcome.Success(listOf(house))

        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChange("lost")
            assertEquals(SearchUiState.Loading, awaitItem())

            viewModel.onQueryChange("house")
            assertEquals(SearchUiState.Success(listOf(house)), awaitItem())

            advanceTimeBy(SEARCH_DEBOUNCE * 2)
            expectNoEvents()
        }
        assertTrue("The outdated search must be cancelled as soon as the query changes", lostSearchCancelled)
    }

    @Test
    fun `given a search in progress when the query is cleared then returns to idle at once`() = runTest {
        coEvery { searchShows("lost") } coAnswers {
            delay(FAST_NETWORK_MILLIS)
            Outcome.Success(listOf(lost))
        }

        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChange("lost")
            assertEquals(SearchUiState.Loading, awaitItem())

            val clearedAt = testScheduler.currentTime
            viewModel.onClearQuery()
            assertEquals(SearchUiState.Idle, awaitItem())
            assertEquals("Idle must not wait for the debounce", clearedAt, testScheduler.currentTime)

            advanceTimeBy(SEARCH_DEBOUNCE * 2)
            expectNoEvents()
        }
    }

    @Test
    fun `given a search without results when finished then the state is empty with the query`() = runTest {
        coEvery { searchShows("zzzz") } returns Outcome.Success(emptyList())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("zzzz")
            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Empty("zzzz"), awaitItem())
        }
    }

    @Test
    fun `given a failing search when retried then searches the same query again`() = runTest {
        coEvery { searchShows("lost") } returns Outcome.Failure(DomainError.Network) andThen
            Outcome.Success(listOf(lost))

        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("lost")
            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Error(DomainError.Network), awaitItem())

            viewModel.onRetry()
            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Success(listOf(lost)), awaitItem())
        }
        coVerify(exactly = 2) { searchShows("lost") }
    }

    @Test
    fun `given results when the query is cleared then returns to idle`() = runTest {
        coEvery { searchShows("lost") } returns Outcome.Success(listOf(lost))

        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("lost")
            awaitItem()
            awaitItem()

            viewModel.onClearQuery()
            assertEquals(SearchUiState.Idle, awaitItem())
        }
        assertEquals("", viewModel.query.value)
    }

    @Test
    fun `given results when the screen resubscribes after the stop timeout then keeps them without searching again`() =
        runTest {
            coEvery { searchShows("lost") } returns Outcome.Success(listOf(lost))

            viewModel.uiState.test {
                awaitItem()
                viewModel.onQueryChange("lost")
                assertEquals(SearchUiState.Loading, awaitItem())
                assertEquals(SearchUiState.Success(listOf(lost)), awaitItem())
            }

            // The user reads a detail screen long enough for a WhileSubscribed upstream to stop.
            advanceTimeBy(10.seconds)

            viewModel.uiState.test {
                assertEquals(SearchUiState.Success(listOf(lost)), awaitItem())
                advanceTimeBy(SEARCH_DEBOUNCE * 2)
                expectNoEvents()
            }
            coVerify(exactly = 1) { searchShows("lost") }
        }

    @Test
    fun `given a query saved before process death when recreated then restores the query and searches it`() = runTest {
        coEvery { searchShows("lost") } returns Outcome.Success(listOf(lost))
        val restored = SearchViewModel(SavedStateHandle(mapOf("query" to "lost")), searchShows)

        assertEquals("lost", restored.query.value)
        restored.uiState.test {
            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Success(listOf(lost)), awaitItem())
        }
        coVerify(exactly = 1) { searchShows("lost") }
    }

    @Test
    fun `given a typed query when changed or cleared then it is kept in the saved state`() {
        viewModel.onQueryChange("lost")
        assertEquals("lost", savedStateHandle.get<String>("query"))

        viewModel.onClearQuery()
        assertEquals("", savedStateHandle.get<String>("query"))
    }

    private companion object {
        const val SLOW_NETWORK_MILLIS = 1_000L
        const val FAST_NETWORK_MILLIS = 100L
    }
}
