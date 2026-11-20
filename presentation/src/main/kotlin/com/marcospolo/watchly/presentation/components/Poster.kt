package com.marcospolo.watchly.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Show poster with a branded fallback (the title's initial) while loading,
 * on error, or when the show has no image.
 *
 * Pass `contentDescription = null` when the poster is decorative because the
 * title is already announced next to it.
 */
@Composable
fun Poster(imageUrl: String?, title: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val accessibility = if (contentDescription == null) {
        Modifier
    } else {
        Modifier.semantics {
            this.contentDescription = contentDescription
            role = Role.Image
        }
    }
    Box(modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer).then(accessibility)) {
        if (imageUrl == null) {
            PosterFallback(title)
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = { PosterFallback(title) },
                error = { PosterFallback(title) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PosterFallback(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.firstOrNull()?.uppercase().orEmpty(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
