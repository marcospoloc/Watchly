package com.marcospolo.watchly.domain.usecase

import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.ShowDetail
import com.marcospolo.watchly.domain.repository.ShowRepository

/** Loads the full information of a single show. */
class GetShowDetailUseCase(private val repository: ShowRepository) {
    suspend operator fun invoke(showId: Int): Outcome<ShowDetail> = repository.getShowDetail(showId)
}
