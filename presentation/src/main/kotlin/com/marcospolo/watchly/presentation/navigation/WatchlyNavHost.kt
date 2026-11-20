package com.marcospolo.watchly.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.marcospolo.watchly.presentation.detail.DetailRoute
import com.marcospolo.watchly.presentation.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchDestination

/** Only the id travels between screens; the detail screen loads (or reads from cache) its own data. */
@Serializable
data class DetailDestination(val showId: Int)

@Composable
fun WatchlyNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SearchDestination) {
        composable<SearchDestination> {
            SearchRoute(onShowClick = { show -> navController.navigate(DetailDestination(show.id)) })
        }
        composable<DetailDestination> {
            DetailRoute(onBack = navController::navigateUp)
        }
    }
}
