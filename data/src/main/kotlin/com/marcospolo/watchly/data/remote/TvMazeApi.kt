package com.marcospolo.watchly.data.remote

import com.marcospolo.watchly.data.remote.dto.SearchResultDto
import com.marcospolo.watchly.data.remote.dto.ShowDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TVMaze public API (https://www.tvmaze.com/api). No API key required. */
internal interface TvMazeApi {
    @GET("search/shows")
    suspend fun searchShows(@Query("q") query: String): List<SearchResultDto>

    @GET("shows/{id}")
    suspend fun getShow(@Path("id") showId: Int): ShowDto
}
