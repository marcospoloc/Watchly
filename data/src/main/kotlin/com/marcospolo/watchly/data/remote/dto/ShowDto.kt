package com.marcospolo.watchly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchResultDto(
    val show: ShowDto,
    /** TVMaze's relevance of this result for the query; higher is better. */
    val score: Double = 0.0
)

@Serializable
internal data class ShowDto(
    val id: Int,
    val name: String,
    val genres: List<String> = emptyList(),
    val premiered: String? = null,
    val language: String? = null,
    val status: String? = null,
    val runtime: Int? = null,
    val summary: String? = null,
    val rating: RatingDto? = null,
    val image: ImageDto? = null,
    val network: ChannelDto? = null,
    val webChannel: ChannelDto? = null
)

@Serializable
internal data class RatingDto(val average: Double? = null)

@Serializable
internal data class ImageDto(val medium: String? = null, val original: String? = null)

@Serializable
internal data class ChannelDto(val name: String? = null)
