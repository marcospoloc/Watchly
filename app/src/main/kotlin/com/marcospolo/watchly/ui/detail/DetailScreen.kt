package com.marcospolo.watchly.ui.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcospolo.watchly.R
import com.marcospolo.watchly.domain.model.DomainError
import com.marcospolo.watchly.domain.model.ShowDetail
import com.marcospolo.watchly.ui.components.MessageState
import com.marcospolo.watchly.ui.components.Poster
import com.marcospolo.watchly.ui.components.formatRating
import com.marcospolo.watchly.ui.components.messageRes
import com.marcospolo.watchly.ui.theme.WatchlyTheme

private val CONTENT_MAX_WIDTH = 600.dp
private const val POSTER_ASPECT_RATIO = 2f / 3f
private const val POSTER_WIDTH_FRACTION = 0.6f

@Composable
fun DetailRoute(onBack: () -> Unit, viewModel: DetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(uiState = uiState, onBack = onBack, onRetry = viewModel::onRetry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(uiState: DetailUiState, onBack: () -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { DetailTopBar(uiState = uiState, onBack = onBack, scrollBehavior = scrollBehavior) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (uiState) {
                DetailUiState.Loading -> DetailLoading()

                is DetailUiState.Success -> DetailContent(uiState.show)

                is DetailUiState.Error -> MessageState(
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(uiState: DetailUiState, onBack: () -> Unit, scrollBehavior: TopAppBarScrollBehavior) {
    LargeTopAppBar(
        title = {
            val title = (uiState as? DetailUiState.Success)?.show?.name.orEmpty()
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() }
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun DetailLoading() {
    val description = stringResource(R.string.detail_loading)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(show: ShowDetail) {
    Column(
        modifier = Modifier
            .widthIn(max = CONTENT_MAX_WIDTH)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Poster(
            imageUrl = show.imageUrl,
            title = show.name,
            contentDescription = stringResource(R.string.detail_poster_description, show.name),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(POSTER_WIDTH_FRACTION)
                .aspectRatio(POSTER_ASPECT_RATIO)
                .clip(RoundedCornerShape(20.dp))
        )

        if (show.genres.isNotEmpty()) {
            DetailSection(title = stringResource(R.string.detail_section_genres)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    show.genres.forEach { genre -> GenreTag(genre) }
                }
            }
        }

        DetailSection(title = stringResource(R.string.detail_section_info)) {
            InfoTable(show)
        }

        DetailSection(title = stringResource(R.string.detail_section_summary)) {
            Text(
                text = show.summary ?: stringResource(R.string.detail_no_summary),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        content()
    }
}

/** Non-interactive tag: a Chip would announce itself as a button that does nothing. */
@Composable
private fun GenreTag(genre: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = genre,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun InfoTable(show: ShowDetail) {
    val unknown = stringResource(R.string.detail_unknown_value)
    val rows = listOf(
        stringResource(R.string.detail_rating) to
            (show.rating?.let { stringResource(R.string.detail_rating_value, formatRating(it)) } ?: unknown),
        stringResource(R.string.detail_premiered) to (show.premiered ?: unknown),
        stringResource(R.string.detail_network) to (show.network ?: unknown),
        stringResource(R.string.detail_language) to (show.language ?: unknown),
        stringResource(R.string.detail_status) to (show.status ?: unknown),
        stringResource(R.string.detail_runtime) to
            (show.runtimeMinutes?.let { stringResource(R.string.detail_runtime_value, it) } ?: unknown)
    )
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(label = label, value = value)
            }
        }
    }
}

/** Label and value are merged so TalkBack reads "Network, CBS" in a single focus. */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

private val previewDetail = ShowDetail(
    id = 1,
    name = "Under the Dome",
    imageUrl = null,
    summary = "Under the Dome is the story of a small town that is suddenly and inexplicably sealed off " +
        "from the rest of the world by an enormous transparent dome.",
    genres = listOf("Drama", "Science-Fiction", "Thriller"),
    rating = 6.5,
    premiered = "2013-06-24",
    language = "English",
    network = "CBS",
    status = "Ended",
    runtimeMinutes = 60
)

@Preview(name = "Detail")
@Preview(name = "Detail · dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Detail · font 200%", fontScale = 2f)
@Composable
private fun DetailPreview() {
    WatchlyTheme { DetailScreen(DetailUiState.Success(previewDetail), {}, {}) }
}

@Preview(name = "Detail error")
@Composable
private fun DetailErrorPreview() {
    WatchlyTheme { DetailScreen(DetailUiState.Error(DomainError.Network), {}, {}) }
}
