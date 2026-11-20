package com.marcospolo.watchly.data.repository

import com.marcospolo.watchly.data.cache.Cache
import com.marcospolo.watchly.data.mapper.toRelevantShows
import com.marcospolo.watchly.data.mapper.toShowDetail
import com.marcospolo.watchly.data.remote.TvMazeApi
import com.marcospolo.watchly.data.remote.safeApiCall
import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.model.ShowDetail
import com.marcospolo.watchly.domain.model.onSuccess
import com.marcospolo.watchly.domain.repository.ShowRepository

/**
 * Cache-first repository: a valid cached value is returned without a network
 * call; otherwise the API is called and the cache refreshed. Failures are
 * never cached, so the next attempt retries the network.
 */
internal class ShowRepositoryImpl(
    private val api: TvMazeApi,
    private val searchCache: Cache<String, List<Show>>,
    private val detailCache: Cache<Int, ShowDetail>
) : ShowRepository {
    override suspend fun searchShows(query: String): Outcome<List<Show>> {
        val cacheKey = query.trim().lowercase()
        searchCache.get(cacheKey)?.let { cached -> return Outcome.Success(cached) }

        return safeApiCall { api.searchShows(query).toRelevantShows() }
            .onSuccess { shows -> searchCache.put(cacheKey, shows) }
    }

    override suspend fun getShowDetail(showId: Int): Outcome<ShowDetail> {
        detailCache.get(showId)?.let { cached -> return Outcome.Success(cached) }

        return safeApiCall { api.getShow(showId).toShowDetail() }
            .onSuccess { detail -> detailCache.put(showId, detail) }
    }
}
