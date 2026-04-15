package com.ncwd.porkstreams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.ncwd.porkstreams.data.model.Movie
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.Background

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedBanner(
    movie: Movie?,
    modifier: Modifier = Modifier,
    watchButtonModifier: Modifier = Modifier,
    onWatch: () -> Unit = {},
    onDetails: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(Background)
    ) {
        if (movie?.backdropUrl != null) {
            AsyncImage(
                model = movie.backdropUrl,
                contentDescription = movie.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Multi-directional gradient for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xEE000000), Color(0x44000000), Color.Transparent),
                        startX = 0f,
                        endX = 900f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Background)
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .padding(start = 48.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = movie?.displayTitle ?: "Welcome to PorkStreams",
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (movie?.vote_average != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "★ ${"%.1f".format(movie.vote_average)}  •  ${movie.displayDate.take(4)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0C0)
                )
            }
            if (!movie?.overview.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = movie!!.overview!!,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFB0B0C0)
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onWatch,
                    colors = ButtonDefaults.colors(containerColor = AccentRed),
                    shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                    modifier = watchButtonModifier
                ) {
                    Text("  ▶  Watch Now  ", style = MaterialTheme.typography.titleSmall)
                }
                OutlinedButton(
                    onClick = onDetails,
                    shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                ) {
                    Text("  ⓘ  Details  ", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
