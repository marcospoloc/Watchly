package com.marcospolo.watchly.domain.model

/** Full information about a TV show, as shown in the detail screen. */
data class ShowDetail(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val summary: String?,
    val genres: List<String>,
    val rating: Double?,
    val premiered: String?,
    val language: String?,
    val network: String?,
    val status: String?,
    val runtimeMinutes: Int?
)
