package com.ncwd.porkstreams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.local.NowPlaying
import com.ncwd.porkstreams.data.providers.StreamUrlBuilder
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.LocalFirstContentFocus
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.Background
import com.ncwd.porkstreams.ui.theme.CardBackground
import com.ncwd.porkstreams.ui.theme.TextSecondary
import com.ncwd.porkstreams.ui.viewmodel.DetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailScreen(
    container: AppContainer,
    mediaType: String,
    id: Int,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: DetailViewModel = viewModel(
        factory = DetailViewModel.Factory(container, mediaType, id)
    )
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val watchButtonFocus = remember { FocusRequester() }
    val firstContentFocus = LocalFirstContentFocus.current

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            try { watchButtonFocus.requestFocus() } catch (_: Exception) {}
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val details = state.details ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        // Backdrop image as the first item
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(300.dp)
            ) {
                if (details.backdropUrl != null) {
                    AsyncImage(
                        model = details.backdropUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x33000000), Background)
                            )
                        )
                )
            }
        }

        // Poster + title + buttons
        item {
            Row(
                modifier = Modifier.padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AsyncImage(
                    model = details.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(150.dp)
                        .height(225.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = details.displayTitle,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (details.vote_average != null) {
                        Text(
                            text = "★ ${"%.1f".format(details.vote_average)}  •  ${
                                (details.release_date ?: details.first_air_date)?.take(4) ?: ""
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (details.genreString.isNotBlank()) {
                        Text(
                            text = details.genreString,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    if (!details.overview.isNullOrBlank()) {
                        Text(
                            text = details.overview,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Watch / Continue Watching
                        Button(
                            onClick = {
                                details.let { d ->
                                    container.nowPlaying.value = NowPlaying(
                                        tmdbId = id.toString(),
                                        mediaType = mediaType,
                                        title = d.displayTitle,
                                        posterPath = d.poster_path,
                                        season = if (mediaType == "tv") state.selectedSeason else null,
                                        episode = if (mediaType == "tv") state.selectedEpisode else null
                                    )
                                }
                                val url = if (mediaType == "movie") {
                                    StreamUrlBuilder.buildMovieUrl(state.movieProvider, id)
                                } else {
                                    StreamUrlBuilder.buildTvUrl(
                                        state.movieProvider,
                                        id,
                                        state.selectedSeason,
                                        state.selectedEpisode
                                    )
                                }
                                onNavigate(AppRoutes.player(url))
                            },
                            colors = ButtonDefaults.colors(containerColor = AccentRed),
                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                            modifier = Modifier
                                .focusRequester(watchButtonFocus)
                                .focusRequester(firstContentFocus)
                        ) {
                            Text(
                                if (state.watchProgress != null) "Continue Watching" else "Watch",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        // Restart — only shown if there's existing progress
                        if (state.watchProgress != null) {
                            Button(
                                onClick = {
                                    container.nowPlaying.value = NowPlaying(
                                        tmdbId = id.toString(),
                                        mediaType = mediaType,
                                        title = details.displayTitle,
                                        posterPath = details.poster_path,
                                        season = if (mediaType == "tv") 1 else null,
                                        episode = if (mediaType == "tv") 1 else null
                                    )
                                    val url = if (mediaType == "movie") {
                                        StreamUrlBuilder.buildMovieUrl(state.movieProvider, id)
                                    } else {
                                        StreamUrlBuilder.buildTvUrl(state.movieProvider, id, 1, 1)
                                    }
                                    onNavigate(AppRoutes.player(url))
                                },
                                colors = ButtonDefaults.colors(containerColor = CardBackground),
                                shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                            ) {
                                Text("Restart", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        // Watchlist toggle
                        Button(
                            onClick = { scope.launch { viewModel.toggleWatchlist() } },
                            colors = ButtonDefaults.colors(containerColor = CardBackground),
                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                if (state.isInWatchlist) "\u2713 Watchlist" else "+ Watchlist",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }

        // TV season dropdown + episode grid
        if (mediaType == "tv" && state.seasons.isNotEmpty()) {
            item { Spacer(Modifier.height(20.dp)) }
            item {
                var dropdownExpanded by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    Box {
                        Button(
                            onClick = { dropdownExpanded = !dropdownExpanded },
                            colors = ButtonDefaults.colors(containerColor = CardBackground),
                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                "Season ${state.selectedSeason}  \u25BC",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(CardBackground)
                        ) {
                            state.seasons.forEach { season ->
                                DropdownMenuItem(
                                    text = {
                                        androidx.compose.material3.Text(
                                            "Season ${season.season_number}",
                                            color = if (state.selectedSeason == season.season_number)
                                                AccentRed else Color.White
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectSeason(season.season_number)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (state.episodes.isNotEmpty()) {
                item { Spacer(Modifier.height(16.dp)) }
                item {
                    Text(
                        text = "Episodes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
                    )
                }
                items(state.episodes.chunked(5)) { rowEpisodes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        rowEpisodes.forEach { episode ->
                            Card(
                                onClick = {
                                    viewModel.selectEpisode(episode.episode_number)
                                    state.details?.let { d ->
                                        container.nowPlaying.value = NowPlaying(
                                            tmdbId = id.toString(),
                                            mediaType = "tv",
                                            title = d.displayTitle,
                                            posterPath = d.poster_path,
                                            season = state.selectedSeason,
                                            episode = episode.episode_number
                                        )
                                    }
                                    val url = StreamUrlBuilder.buildTvUrl(
                                        state.movieProvider, id,
                                        state.selectedSeason, episode.episode_number
                                    )
                                    onNavigate(AppRoutes.player(url))
                                },
                                modifier = Modifier.weight(1f).height(100.dp),
                                shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
                                colors = CardDefaults.colors(
                                    containerColor = if (state.selectedEpisode == episode.episode_number)
                                        CardBackground.copy(alpha = 0.8f) else CardBackground
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "E${episode.episode_number}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (state.selectedEpisode == episode.episode_number)
                                            AccentRed else TextSecondary
                                    )
                                    Text(
                                        episode.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        // Fill remaining spots in the last row
                        repeat(5 - rowEpisodes.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Cast
        if (state.cast.isNotEmpty()) {
            item { Spacer(Modifier.height(24.dp)) }
            item {
                Text(
                    text = "Cast",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    content = {
                        items(state.cast, key = { it.id }) { member ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(80.dp)
                            ) {
                                AsyncImage(
                                    model = member.profileUrl,
                                    contentDescription = member.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(CardBackground)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                )
            }
        }


        item { Spacer(Modifier.height(48.dp)) }
    }
}
