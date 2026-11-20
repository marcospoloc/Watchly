package com.marcospolo.watchly.data.mapper

import com.marcospolo.watchly.data.remote.dto.SearchResultDto
import com.marcospolo.watchly.data.remote.dto.ShowDto
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.model.ShowDetail

private const val YEAR_LENGTH = 4
private val PARAGRAPH_BREAK = Regex("</p>\\s*<p>|<br\\s*/?>", RegexOption.IGNORE_CASE)
private val HTML_TAG = Regex("<[^>]*>")
private val HTML_ENTITIES = mapOf(
    "&amp;" to "&",
    "&quot;" to "\"",
    "&#39;" to "'",
    "&apos;" to "'",
    "&lt;" to "<",
    "&gt;" to ">",
    "&nbsp;" to " "
)

/**
 * Matches every known entity at once, so each one is replaced a single time:
 * decoding them one by one would turn "&amp;lt;" into "&lt;" and then into "<".
 */
private val HTML_ENTITY = Regex(HTML_ENTITIES.keys.joinToString("|") { Regex.escape(it) })

/**
 * TVMaze search is fuzzy: besides the real matches it returns shows that only
 * look similar ("breaking bad" also brings "Baking Mad"). Results scoring less
 * than this fraction of the best one are dropped as noise.
 */
private const val MIN_RELATIVE_SCORE = 0.5

/** Maps search results to shows, keeping the API order and dropping low-relevance noise. */
internal fun List<SearchResultDto>.toRelevantShows(): List<Show> {
    val minScore = (maxOfOrNull { it.score } ?: 0.0) * MIN_RELATIVE_SCORE
    return filter { it.score >= minScore }.map { it.show.toShow() }
}

internal fun ShowDto.toShow(): Show = Show(
    id = id,
    name = name,
    posterUrl = image?.medium ?: image?.original,
    premieredYear = premiered?.take(YEAR_LENGTH)?.toIntOrNull(),
    genres = genres,
    rating = rating?.average
)

internal fun ShowDto.toShowDetail(): ShowDetail = ShowDetail(
    id = id,
    name = name,
    imageUrl = image?.original ?: image?.medium,
    summary = summary?.toPlainText()?.takeIf { it.isNotBlank() },
    genres = genres,
    rating = rating?.average,
    premiered = premiered,
    language = language,
    network = network?.name ?: webChannel?.name,
    status = status,
    runtimeMinutes = runtime
)

/** TVMaze summaries are HTML fragments; screen readers and Text need plain text. */
internal fun String.toPlainText(): String {
    val withoutTags = replace(PARAGRAPH_BREAK, "\n\n").replace(HTML_TAG, "")
    return withoutTags.replace(HTML_ENTITY) { HTML_ENTITIES.getValue(it.value) }.trim()
}
