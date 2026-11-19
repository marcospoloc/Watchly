package com.marcospolo.watchly.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.marcospolo.watchly.MainDispatcherRule
import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.ShowDetail
import com.marcospolo.watchly.domain.usecase.GetShowDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getShowDetail: GetShowDetailUseCase = mockk()

    private val detail = ShowDetail(
        id = 7,
        name = "Lost",
        imageUrl = null,
        summary = null,
        genres = emptyList(),
        rating = null,
        premiered = null,
        language = null,
        network = null,
        status = null,
        runtimeMinutes = null
    )

    private fun createViewModel() = DetailViewModel(SavedStateHandle(mapOf(SHOW_ID_ARG to 7)), getShowDetail)

    @Test
    fun `given a show id in the navigation arguments when created then loads that show`() = runTest {
        coEvery { getShowDetail(7) } returns Outcome.Success(detail)

        val viewModel = createViewModel()
        assertEquals(DetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()

        assertEquals(DetailUiState.Success(detail), viewModel.uiState.value)
        coVerify(exactly = 1) { getShowDetail(7) }
    }

    @Test
    fun `given a failure when loading then exposes the error and retry loads again`() = runTest {
        coEvery { getShowDetail(7) } returns Outcome.Failure(DomainError.Network) andThen Outcome.Success(detail)

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(DetailUiState.Error(DomainError.Network), viewModel.uiState.value)

        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(DetailUiState.Success(detail), viewModel.uiState.value)
        coVerify(exactly = 2) { getShowDetail(7) }
    }
}
