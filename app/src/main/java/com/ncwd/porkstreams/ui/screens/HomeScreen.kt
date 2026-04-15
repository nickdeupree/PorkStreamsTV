package com.ncwd.porkstreams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.local.NowPlaying
import com.ncwd.porkstreams.data.providers.StreamUrlBuilder
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.LocalFirstContentFocus
import com.ncwd.porkstreams.ui.components.ContinueWatchingRow
import com.ncwd.porkstreams.ui.components.FeaturedBanner
import com.ncwd.porkstreams.ui.components.MovieRow
import com.ncwd.porkstreams.ui.components.PpvStreamRow
import com.ncwd.porkstreams.ui.components.SportMatchRow
import com.ncwd.porkstreams.ui.components.TvShowRow
import com.ncwd.porkstreams.ui.components.WatchLaterRow
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.Background
import com.ncwd.porkstreams.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    container: AppContainer,
    onNavigate: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(container))
    val state by viewModel.state.collectAsState()
    val firstFocus = LocalFirstContentFocus.current

    // "type:id" e.g. "movie:123" or "tv:456"
    var lastDetailedKey by remember { mutableStateOf<String?>(null) }
    val lastDetailedFocusRequester = remember { FocusRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED && lastDetailedKey != null) {
            repeat(10) {
                try {
                    lastDetailedFocusRequester.requestFocus()
                    return@LaunchedEffect
                } catch (_: Exception) {
                    delay(16)
                }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentRed)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        // Featured banner — always first
        item {
            FeaturedBanner(
                movie = state.featuredMovie,
                watchButtonModifier = Modifier.focusRequester(firstFocus),
                onWatch = {
                    state.featuredMovie?.let { movie ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = movie.id.toString(),
                            mediaType = "movie",
                            title = movie.displayTitle,
                            posterPath = movie.poster_path
                        )
                        onNavigate(AppRoutes.player(StreamUrlBuilder.buildMovieUrl(state.movieProvider, movie.id)))
                    }
                },
                onDetails = {
                    state.featuredMovie?.let { movie ->
                        lastDetailedKey = "movie:${movie.id}"
                        onNavigate(AppRoutes.detail("movie", movie.id))
                    }
                }
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        // 1. Continue Watching
        if (state.continueWatching.isNotEmpty()) {
            item {
                ContinueWatchingRow(
                    items = state.continueWatching,
                    highlightedTmdbId = lastDetailedKey?.substringAfter(":"),
                    highlightedItemFocusRequester = lastDetailedFocusRequester,
                    onWatch = { progress ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = progress.tmdbId,
                            mediaType = progress.type,
                            title = progress.title,
                            posterPath = progress.posterPath,
                            season = progress.season,
                            episode = progress.episode
                        )
                        val url = if (progress.type == "movie") {
                            StreamUrlBuilder.buildMovieUrl(state.movieProvider, progress.tmdbId.toInt())
                        } else {
                            StreamUrlBuilder.buildTvUrl(
                                state.movieProvider,
                                progress.tmdbId.toInt(),
                                progress.season ?: 1,
                                progress.episode ?: 1
                            )
                        }
                        onNavigate(AppRoutes.player(url))
                    },
                    onRestart = { progress ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = progress.tmdbId,
                            mediaType = progress.type,
                            title = progress.title,
                            posterPath = progress.posterPath,
                            season = if (progress.type == "tv") 1 else null,
                            episode = if (progress.type == "tv") 1 else null
                        )
                        val url = if (progress.type == "movie") {
                            StreamUrlBuilder.buildMovieUrl(state.movieProvider, progress.tmdbId.toInt())
                        } else {
                            StreamUrlBuilder.buildTvUrl(state.movieProvider, progress.tmdbId.toInt(), 1, 1)
                        }
                        onNavigate(AppRoutes.player(url))
                    },
                    onDetails = { progress ->
                        lastDetailedKey = "${progress.type}:${progress.tmdbId}"
                        onNavigate(AppRoutes.detail(progress.type, progress.tmdbId.toInt()))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        // 2. Live Sports
        if (state.liveMatches.isNotEmpty()) {
            item {
                SportMatchRow(
                    title = "Live Sports",
                    matches = state.liveMatches.take(8),
                    onClick = { match ->
                        val url = match.firstStreamUrl ?: return@SportMatchRow
                        onNavigate(AppRoutes.player(url))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (state.livePpvStreams.isNotEmpty()) {
            item {
                PpvStreamRow(
                    title = "Live Sports",
                    streams = state.livePpvStreams.take(8),
                    onClick = { stream ->
                        val url = stream.iframe ?: return@PpvStreamRow
                        onNavigate(AppRoutes.player(url))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        // 3. Watchlist
        if (state.watchLater.isNotEmpty()) {
            item {
                WatchLaterRow(
                    items = state.watchLater,
                    highlightedTmdbId = lastDetailedKey?.substringAfter(":"),
                    highlightedItemFocusRequester = lastDetailedFocusRequester,
                    onWatch = { item ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = item.tmdbId,
                            mediaType = item.type,
                            title = item.title,
                            posterPath = item.posterPath,
                            season = if (item.type == "tv") 1 else null,
                            episode = if (item.type == "tv") 1 else null
                        )
                        val url = if (item.type == "movie") {
                            StreamUrlBuilder.buildMovieUrl(state.movieProvider, item.tmdbId.toInt())
                        } else {
                            StreamUrlBuilder.buildTvUrl(state.movieProvider, item.tmdbId.toInt(), 1, 1)
                        }
                        onNavigate(AppRoutes.player(url))
                    },
                    onDetails = { item ->
                        lastDetailedKey = "${item.type}:${item.tmdbId}"
                        onNavigate(AppRoutes.detail(item.type, item.tmdbId.toInt()))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        // 4. Trending Movies
        if (state.trendingMovies.isNotEmpty()) {
            item {
                val lastDetailedMovieId = lastDetailedKey?.let {
                    if (it.startsWith("movie:")) it.substringAfter(":").toIntOrNull() else null
                }
                MovieRow(
                    title = "Trending Movies",
                    movies = state.trendingMovies,
                    highlightedItemId = lastDetailedMovieId,
                    highlightedItemFocusRequester = lastDetailedFocusRequester,
                    onWatch = { movie ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = movie.id.toString(),
                            mediaType = "movie",
                            title = movie.displayTitle,
                            posterPath = movie.poster_path
                        )
                        onNavigate(AppRoutes.player(StreamUrlBuilder.buildMovieUrl(state.movieProvider, movie.id)))
                    },
                    onDetails = { movie ->
                        lastDetailedKey = "movie:${movie.id}"
                        onNavigate(AppRoutes.detail("movie", movie.id))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        // 5. Trending TV
        if (state.trendingTvShows.isNotEmpty()) {
            item {
                val lastDetailedTvId = lastDetailedKey?.let {
                    if (it.startsWith("tv:")) it.substringAfter(":").toIntOrNull() else null
                }
                TvShowRow(
                    title = "Trending TV Shows",
                    shows = state.trendingTvShows,
                    highlightedItemId = lastDetailedTvId,
                    highlightedItemFocusRequester = lastDetailedFocusRequester,
                    onWatch = { show ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = show.id.toString(),
                            mediaType = "tv",
                            title = show.name,
                            posterPath = show.poster_path,
                            season = 1,
                            episode = 1
                        )
                        onNavigate(AppRoutes.player(StreamUrlBuilder.buildTvUrl(state.movieProvider, show.id, 1, 1)))
                    },
                    onDetails = { show ->
                        lastDetailedKey = "tv:${show.id}"
                        onNavigate(AppRoutes.detail("tv", show.id))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}
