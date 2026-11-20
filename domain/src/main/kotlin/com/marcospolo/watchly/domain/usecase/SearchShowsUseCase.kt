package com.marcospolo.watchly.domain.usecase

import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.repository.ShowRepository

/**
 * Searches shows by name.
 *
 * Owns the business rules of a search: the query is normalized and a blank
 * query yields no results without hitting the repository.
 *
 * The search API matches whole words, so while the user is still typing the
 * last word ("breaking b") it finds nothing or unrelated shows. When none of
 * the results starts with the typed text, the last word is completed locally:
 * the query is searched again without it ("breaking") and the shows whose name
 * starts with the typed text at a word boundary are listed first.
 */
class SearchShowsUseCase(private val repository: ShowRepository) {
    suspend operator fun invoke(rawQuery: String): Outcome<List<Show>> {
        val query = normalizeQuery(rawQuery)
        if (query.isEmpty()) return Outcome.Success(emptyList())

        return when (val result = repository.searchShows(query)) {
            is Outcome.Success -> completeLastWord(query, result.value)
            is Outcome.Failure -> result
        }
    }

    private suspend fun completeLastWord(query: String, shows: List<Show>): Outcome<List<Show>> {
        val completedWords = query.substringBeforeLast(' ', missingDelimiterValue = "")
        val matchesQuery = wordPrefixMatcher(query)
        if (completedWords.isEmpty() || shows.any(matchesQuery)) return Outcome.Success(shows)

        return when (val completion = repository.searchShows(completedWords)) {
            is Outcome.Success -> {
                val completedMatches = completion.value.filter(matchesQuery)
                Outcome.Success((completedMatches + shows).distinctBy { it.id })
            }

            is Outcome.Failure -> completion
        }
    }

    private fun normalizeQuery(rawQuery: String): String = rawQuery.trim().replace(WHITESPACE, " ")

    /** True when the show name contains [query] at the start of a word, ignoring case. */
    private fun wordPrefixMatcher(query: String): (Show) -> Boolean {
        val pattern = Regex("(^|[^\\p{L}\\p{N}])" + Regex.escape(query), RegexOption.IGNORE_CASE)
        return { show -> pattern.containsMatchIn(show.name) }
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
