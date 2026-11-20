package com.marcospolo.watchly.data.remote

import com.marcospolo.watchly.data.remote.dto.SearchResultDto
import com.marcospolo.watchly.data.remote.dto.ShowDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TVMaze public API (https://www.tvmaze.com/api). No API key required. */
internal interface TvMazeApi {
    /**
     * How TVMaze search behaves, which the search rules depend on:
     * - It matches whole words, so a word still being typed ("breaking b") finds
     *   nothing or unrelated shows. `SearchShowsUseCase` completes the last word.
     * - It is fuzzy and every result carries a relevance `score`; shows that only
     *   look similar come back too. `toRelevantShows` drops that noise.
     * - It returns at most 10 results and has no pagination.
     */
    @GET("search/shows")
    suspend fun searchShows(@Query("q") query: String): List<SearchResultDto>

    @GET("shows/{id}")
    suspend fun getShow(@Path("id") showId: Int): ShowDto
}
