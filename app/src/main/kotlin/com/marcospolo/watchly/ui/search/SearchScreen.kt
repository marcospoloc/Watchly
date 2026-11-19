package com.marcospolo.watchly.ui.search

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcospolo.watchly.R
import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.ui.components.MessageState
import com.marcospolo.watchly.ui.components.messageRes
import com.marcospolo.watchly.ui.theme.WatchlyTheme

/** Results use one column on phones and as many 340dp columns as fit on tablets/landscape. */
private val RESULT_MIN_WIDTH = 340.dp
private const val SKELETON_ITEMS = 6

/** Connects the [SearchViewModel] to the stateless [SearchScreen]. */
@Composable
fun SearchRoute(onShowClick: (Show) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::onClearQuery,
        onRetry = viewModel::onRetry,
        onShowClick = onShowClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onShowClick: (Show) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.semantics { heading() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                onClearQuery = onClearQuery,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            SearchContent(uiState = uiState, onRetry = onRetry, onShowClick = onShowClick)
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Focus the field only on first launch, not every time the user comes back from a detail.
    var hasRequestedInitialFocus by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasRequestedInitialFocus) {
            focusRequester.requestFocus()
            hasRequestedInitialFocus = true
        }
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        label = { Text(stringResource(R.string.search_label)) },
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.search_clear))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
    )
}

@Composable
private fun SearchContent(uiState: SearchUiState, onRetry: () -> Unit, onShowClick: (Show) -> Unit) {
    when (uiState) {
        SearchUiState.Idle -> MessageState(
            icon = Icons.Filled.Search,
            title = stringResource(R.string.search_idle_title),
            body = stringResource(R.string.search_idle_body)
        )

        SearchUiState.Loading -> LoadingResults()

        is SearchUiState.Success -> SearchResults(shows = uiState.shows, onShowClick = onShowClick)

        is SearchUiState.Empty -> MessageState(
            icon = Icons.Filled.Search,
            title = stringResource(R.string.search_empty_title),
            body = stringResource(R.string.search_empty_body, uiState.query)
        )

        is SearchUiState.Error -> MessageState(
            icon = Icons.Filled.Warning,
            title = stringResource(R.string.error_title),
            body = stringResource(uiState.error.messageRes()),
            iconContainerColor = MaterialTheme.colorScheme.errorContainer,
            iconColor = MaterialTheme.colorScheme.onErrorContainer,
            actionLabel = stringResource(R.string.action_retry),
            onAction = onRetry
        )
    }
}

@Composable
private fun SearchResults(shows: List<Show>, onShowClick: (Show) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(RESULT_MIN_WIDTH),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = pluralStringResource(R.plurals.search_results_count, shows.size, shows.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // Announces "12 results" when a search finishes.
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        items(items = shows, key = { it.id }) { show ->
            ShowListItem(show = show, onClick = { onShowClick(show) })
        }
    }
}

/** Skeleton placeholders: hidden from TalkBack, which hears a single "Searching…" instead. */
@Composable
private fun LoadingResults() {
    val loadingDescription = stringResource(R.string.search_loading)
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clearAndSetSemantics {
                contentDescription = loadingDescription
                liveRegion = LiveRegionMode.Polite
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(SKELETON_ITEMS) { SkeletonItem(Modifier.alpha(pulse)) }
    }
}

@Composable
private fun SkeletonItem(modifier: Modifier = Modifier) {
    val placeholder = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            Modifier
                .width(72.dp)
                .aspectRatio(2f / 3f)
                .background(placeholder, RoundedCornerShape(10.dp))
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            Box(Modifier.fillMaxWidth(0.6f).height(18.dp).background(placeholder, RoundedCornerShape(4.dp)))
            Box(Modifier.fillMaxWidth(0.3f).height(14.dp).background(placeholder, RoundedCornerShape(4.dp)))
            Box(Modifier.fillMaxWidth(0.45f).height(14.dp).background(placeholder, RoundedCornerShape(4.dp)))
        }
    }
}

private val previewShows = listOf(
    Show(1, "Breaking Bad", null, 2008, listOf("Drama", "Crime", "Thriller"), 9.2),
    Show(2, "Lost", null, 2004, listOf("Drama", "Adventure"), 8.3),
    Show(3, "A show with a very long title that needs to wrap on small screens", null, null, emptyList(), null)
)

@Preview(name = "Results")
@Preview(name = "Results · dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Results · font 200%", fontScale = 2f)
@Composable
private fun SearchResultsPreview() {
    WatchlyTheme {
        SearchScreen("lost", SearchUiState.Success(previewShows), {}, {}, {}, {})
    }
}

@Preview(name = "Idle")
@Composable
private fun SearchIdlePreview() {
    WatchlyTheme { SearchScreen("", SearchUiState.Idle, {}, {}, {}, {}) }
}

@Preview(name = "Loading")
@Composable
private fun SearchLoadingPreview() {
    WatchlyTheme { SearchScreen("lost", SearchUiState.Loading, {}, {}, {}, {}) }
}

@Preview(name = "Error · dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchErrorPreview() {
    WatchlyTheme { SearchScreen("lost", SearchUiState.Error(DomainError.Network), {}, {}, {}, {}) }
}
