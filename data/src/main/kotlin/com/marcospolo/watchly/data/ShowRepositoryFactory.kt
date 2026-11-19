package com.marcospolo.watchly.data

import com.marcospolo.watchly.data.cache.InMemoryCache
import com.marcospolo.watchly.data.remote.TvMazeApi
import com.marcospolo.watchly.data.repository.ShowRepositoryImpl
import com.marcospolo.watchly.domain.repository.ShowRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

const val TVMAZE_BASE_URL = "https://api.tvmaze.com/"

/** How long a search or detail result is served from memory before going back to the network. */
val DEFAULT_CACHE_TTL: Duration = 3.minutes

private val json = Json { ignoreUnknownKeys = true }

/**
 * Single public entry point of the data layer. Everything else (Retrofit API,
 * DTOs, mappers, repository implementation) stays `internal`, so the app only
 * depends on the domain [ShowRepository] contract.
 */
fun createShowRepository(
    httpClient: OkHttpClient,
    baseUrl: String = TVMAZE_BASE_URL,
    cacheTtl: Duration = DEFAULT_CACHE_TTL
): ShowRepository {
    val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(TvMazeApi::class.java)

    return ShowRepositoryImpl(
        api = api,
        searchCache = InMemoryCache(cacheTtl),
        detailCache = InMemoryCache(cacheTtl)
    )
}
