package com.marcospolo.watchly.domain.usecase

import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.repository.ShowRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchShowsUseCaseTest {
    private val repository: ShowRepository = mockk()
    private val searchShows = SearchShowsUseCase(repository)

    private val lost = Show(
        id = 123,
        name = "Lost",
        posterUrl = null,
        premieredYear = 2004,
        genres = listOf("Drama"),
        rating = 8.3
    )

    @Test
    fun `given a query when invoked then delegates to the repository and returns its result`() = runTest {
        coEvery { repository.searchShows("lost") } returns Outcome.Success(listOf(lost))

        val result = searchShows("lost")

        assertEquals(Outcome.Success(listOf(lost)), result)
        coVerify(exactly = 1) { repository.searchShows("lost") }
    }

    @Test
    fun `given surrounding and repeated whitespace when invoked then passes a normalized query`() = runTest {
        coEvery { repository.searchShows(any()) } returns Outcome.Success(emptyList())

        searchShows("  the   office ")

        coVerify(exactly = 1) { repository.searchShows("the office") }
    }

    @Test
    fun `given a blank query when invoked then returns no results without calling the repository`() = runTest {
        val result = searchShows("   ")

        assertEquals(Outcome.Success(emptyList<Show>()), result)
        coVerify(exactly = 0) { repository.searchShows(any()) }
    }

    @Test
    fun `given the repository fails when invoked then propagates the error`() = runTest {
        coEvery { repository.searchShows("lost") } returns Outcome.Failure(DomainError.Network)

        val result = searchShows("lost")

        assertEquals(Outcome.Failure(DomainError.Network), result)
    }
}
