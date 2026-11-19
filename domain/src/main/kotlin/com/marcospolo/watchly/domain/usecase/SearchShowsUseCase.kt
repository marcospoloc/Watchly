package com.marcospolo.watchly.domain.usecase

import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.repository.ShowRepository

/**
 * Searches shows by name.
 *
 * Owns the business rules of a search: the query is normalized and a blank
 * query yields no results without hitting the repository.
 */
class SearchShowsUseCase(private val repository: ShowRepository) {
    suspend operator fun invoke(rawQuery: String): Outcome<List<Show>> {
        val query = normalizeQuery(rawQuery)
        if (query.isEmpty()) return Outcome.Success(emptyList())
        return repository.searchShows(query)
    }

    private fun normalizeQuery(rawQuery: String): String = rawQuery.trim().replace(WHITESPACE, " ")

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
