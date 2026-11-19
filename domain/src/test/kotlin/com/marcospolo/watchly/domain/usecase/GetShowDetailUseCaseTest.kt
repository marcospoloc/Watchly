package com.marcospolo.watchly.domain.usecase

import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.ShowDetail
import com.marcospolo.watchly.domain.repository.ShowRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetShowDetailUseCaseTest {
    private val repository: ShowRepository = mockk()
    private val getShowDetail = GetShowDetailUseCase(repository)

    private val detail = ShowDetail(
        id = 1,
        name = "Under the Dome",
        imageUrl = null,
        summary = "A town is trapped under a dome.",
        genres = listOf("Drama", "Science-Fiction"),
        rating = 6.5,
        premiered = "2013-06-24",
        language = "English",
        network = "CBS",
        status = "Ended",
        runtimeMinutes = 60
    )

    @Test
    fun `given a show id when invoked then returns the repository detail`() = runTest {
        coEvery { repository.getShowDetail(1) } returns Outcome.Success(detail)

        val result = getShowDetail(1)

        assertEquals(Outcome.Success(detail), result)
        coVerify(exactly = 1) { repository.getShowDetail(1) }
    }

    @Test
    fun `given the repository fails when invoked then propagates the error`() = runTest {
        coEvery { repository.getShowDetail(99) } returns Outcome.Failure(DomainError.NotFound)

        val result = getShowDetail(99)

        assertEquals(Outcome.Failure(DomainError.NotFound), result)
    }
}
