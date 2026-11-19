package com.marcospolo.watchly.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.marcospolo.watchly.R
import com.marcospolo.watchly.domain.model.Show
import com.marcospolo.watchly.ui.components.Poster
import com.marcospolo.watchly.ui.components.RatingBadge

private const val POSTER_ASPECT_RATIO = 2f / 3f

/**
 * One search result. `clickable` merges the children into a single TalkBack
 * focus ("Lost, 2004, Drama · Adventure, Rating 8.3 out of 10, double tap to
 * open details"), so a result is one swipe instead of four.
 */
@Composable
fun ShowListItem(show: Show, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = stringResource(R.string.show_item_open_detail),
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Poster(
                imageUrl = show.posterUrl,
                title = show.name,
                contentDescription = null,
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(POSTER_ASPECT_RATIO)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = show.name, style = MaterialTheme.typography.titleMedium)
                show.premieredYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (show.genres.isNotEmpty()) {
                    Text(
                        text = show.genres.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                show.rating?.let { rating -> RatingBadge(rating = rating, modifier = Modifier.padding(top = 4.dp)) }
            }
        }
    }
}
