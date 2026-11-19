package com.marcospolo.watchly.domain.repository

import com.marcospolo.watchly.domain.model.Outcome
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.domain.model.ShowDetail

/** Contract owned by the domain and implemented by the data layer (dependency inversion). */
interface ShowRepository {
    suspend fun searchShows(query: String): Outcome<List<Show>>

    suspend fun getShowDetail(showId: Int): Outcome<ShowDetail>
}
