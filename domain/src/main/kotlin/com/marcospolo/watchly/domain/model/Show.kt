package com.marcospolo.watchly.domain.model

/** Summary of a TV show, as shown in a search result list. */
data class Show(
    val id: Int,
    val name: String,
    val posterUrl: String?,
    val premieredYear: Int?,
    val genres: List<String>,
    val rating: Double?
)
