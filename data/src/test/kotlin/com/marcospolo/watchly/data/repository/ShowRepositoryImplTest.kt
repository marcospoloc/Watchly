package com.marcospolo.watchly.data.repository

import com.marcospolo.watchly.data.cache.InMemoryCache
import com.marcospolo.watchly.data.remote.TvMazeApi
import com.marcospolo.watchly.data.remote.dto.SearchResultDto
import com.marcospolo.watchly.data.remote.dto.ShowDto
import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.model.ShowDetail
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TestTimeSource
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ShowRepositoryImplTest {
    private val api: TvMazeApi = mockk()
    private val timeSource = TestTimeSource()
    private val repository = ShowRepositoryImpl(
        api = api,
        searchCache = InMemoryCache<String, List<Show>>(ttl = CACHE_TTL, timeSource = timeSource),
        detailCache = InMemoryCache<Int, ShowDetail>(ttl = CACHE_TTL, timeSource = timeSource)
    )

    private val lostDto = ShowDto(id = 123, name = "Lost", premiered = "2004-09-22")
    private val lostRebootDto = ShowDto(id = 999, name = "Lost (2026)")

    @Test
    fun `given a cached query within the ttl when searched again then does not call the network`() = runTest {
        coEvery { api.searchShows("lost") } returns listOf(SearchResultDto(lostDto))

        val first = repository.searchShows("lost")
        timeSource += 2.minutes
        val second = repository.searchShows("lost")

        assertEquals(first, second)
        assertEquals(listOf("Lost"), (second as Outcome.Success).value.map { it.name })
        coVerify(exactly = 1) { api.searchShows(any()) }
    }

    @Test
    fun `given queries that differ only in case and spaces when searched then they share the cache entry`() = runTest {
        coEvery { api.searchShows(any()) } returns listOf(SearchResultDto(lostDto))

        repository.searchShows("Lost")
        repository.searchShows(" lost ")

        coVerify(exactly = 1) { api.searchShows(any()) }
    }

    @Test
    fun `given an expired cache entry when searched then fetches from network and refreshes the cache`() = runTest {
        coEvery { api.searchShows("lost") } returnsMany listOf(
            listOf(SearchResultDto(lostDto)),
            listOf(SearchResultDto(lostRebootDto))
        )
        repository.searchShows("lost")

        timeSource += CACHE_TTL
        val afterExpiry = repository.searchShows("lost")
        val fromRefreshedCache = repository.searchShows("lost")

        val expected = Outcome.Success(listOf(lostRebootDto.name))
        assertEquals(expected, afterExpiry.names())
        assertEquals(expected, fromRefreshedCache.names())
        coVerify(exactly = 2) { api.searchShows("lost") }
    }

    @Test
    fun `given different queries when searched then each one calls the network`() = runTest {
        coEvery { api.searchShows(any()) } returns emptyList()

        repository.searchShows("lost")
        repository.searchShows("house")

        coVerify(exactly = 1) { api.searchShows("lost") }
        coVerify(exactly = 1) { api.searchShows("house") }
    }

    @Test
    fun `given a network failure when searched then returns a network error and does not cache it`() = runTest {
        coEvery { api.searchShows("lost") } throws IOException("offline") andThen listOf(SearchResultDto(lostDto))

        val failure = repository.searchShows("lost")
        val retry = repository.searchShows("lost")

        assertEquals(Outcome.Failure(DomainError.Network), failure)
        assertEquals(Outcome.Success(listOf("Lost")), retry.names())
        coVerify(exactly = 2) { api.searchShows("lost") }
    }

    @Test
    fun `given a cached detail within the ttl when requested again then does not call the network`() = runTest {
        coEvery { api.getShow(123) } returns lostDto

        repository.getShowDetail(123)
        val second = repository.getShowDetail(123)

        assertEquals("Lost", (second as Outcome.Success).value.name)
        coVerify(exactly = 1) { api.getShow(123) }
    }

    @Test
    fun `given an unknown show when requested then returns not found`() = runTest {
        coEvery { api.getShow(404) } throws httpError(404)

        assertEquals(Outcome.Failure(DomainError.NotFound), repository.getShowDetail(404))
    }

    @Test
    fun `given a server error when requested then returns server error`() = runTest {
        coEvery { api.getShow(1) } throws httpError(500)

        assertEquals(Outcome.Failure(DomainError.Server), repository.getShowDetail(1))
    }

    private fun Outcome<List<Show>>.names(): Outcome<List<String>> = when (this) {
        is Outcome.Success -> Outcome.Success(value.map { it.name })
        is Outcome.Failure -> this
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody(null)))

    private companion object {
        val CACHE_TTL = 3.minutes
    }
}
