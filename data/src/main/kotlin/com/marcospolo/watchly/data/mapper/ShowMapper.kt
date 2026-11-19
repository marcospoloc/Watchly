package com.marcospolo.watchly.data.mapper

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
    return HTML_ENTITIES.entries
        .fold(withoutTags) { text, (entity, character) -> text.replace(entity, character) }
        .trim()
}
