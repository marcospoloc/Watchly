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
    private val breakingBad = show(1, "Breaking Bad")
    private val breakingIn = show(2, "Breaking In")
    private val breakingBear = show(3, "Breaking Bear")
    private val bangkokBreaking = show(4, "Bangkok Breaking")
    private val gameOfThrones = show(5, "Game of Thrones")
    private val gameOfSilence = show(6, "Game of Silence")
    private val nameOfTheGame = show(7, "The Name of the Game")

    private fun show(id: Int, name: String) = Show(id, name, null, null, emptyList(), null)

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
    fun `given several words without results when invoked then completes the last word with a shorter search`() =
        runTest {
            coEvery { repository.searchShows("Breaking b") } returns Outcome.Success(emptyList())
            coEvery { repository.searchShows("Breaking") } returns
                Outcome.Success(listOf(breakingBad, breakingIn, breakingBear, bangkokBreaking))

            val result = searchShows("Breaking b")

            assertEquals(Outcome.Success(listOf(breakingBad, breakingBear)), result)
            coVerify(exactly = 1) { repository.searchShows("Breaking") }
        }

    @Test
    fun `given results that do not match the typed words when invoked then completed matches come first`() = runTest {
        coEvery { repository.searchShows("game of thr") } returns Outcome.Success(listOf(nameOfTheGame))
        coEvery { repository.searchShows("game of") } returns
            Outcome.Success(listOf(gameOfThrones, gameOfSilence))

        val result = searchShows("game of thr")

        assertEquals(Outcome.Success(listOf(gameOfThrones, nameOfTheGame)), result)
    }

    @Test
    fun `given results that already match the typed words when invoked then does not search again`() = runTest {
        coEvery { repository.searchShows("breaking bad") } returns Outcome.Success(listOf(breakingBad))

        val result = searchShows("breaking bad")

        assertEquals(Outcome.Success(listOf(breakingBad)), result)
        coVerify(exactly = 1) { repository.searchShows(any()) }
    }

    @Test
    fun `given a single word without results when invoked then does not search again`() = runTest {
        coEvery { repository.searchShows("zzzz") } returns Outcome.Success(emptyList())

        val result = searchShows("zzzz")

        assertEquals(Outcome.Success(emptyList<Show>()), result)
        coVerify(exactly = 1) { repository.searchShows(any()) }
    }

    @Test
    fun `given the completing search fails when invoked then propagates the error`() = runTest {
        coEvery { repository.searchShows("breaking b") } returns Outcome.Success(emptyList())
        coEvery { repository.searchShows("breaking") } returns Outcome.Failure(DomainError.Network)

        val result = searchShows("breaking b")

        assertEquals(Outcome.Failure(DomainError.Network), result)
    }

    @Test
    fun `given the completing search repeats a show when invoked then it appears only once`() = runTest {
        coEvery { repository.searchShows("game of thr") } returns Outcome.Success(emptyList())
        coEvery { repository.searchShows("game of") } returns Outcome.Success(listOf(gameOfThrones, gameOfThrones))

        val result = searchShows("game of thr")

        assertEquals(Outcome.Success(listOf(gameOfThrones)), result)
    }

    @Test
    fun `given the repository fails when invoked then propagates the error`() = runTest {
        coEvery { repository.searchShows("lost") } returns Outcome.Failure(DomainError.Network)

        val result = searchShows("lost")

        assertEquals(Outcome.Failure(DomainError.Network), result)
    }
}
