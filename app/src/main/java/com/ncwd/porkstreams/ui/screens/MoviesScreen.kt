package com.ncwd.porkstreams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.ncwd.porkstreams.ui.components.MovieRow
import com.ncwd.porkstreams.ui.components.WatchLaterRow
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.Background
import com.ncwd.porkstreams.ui.viewmodel.MoviesViewModel

@Composable
fun MoviesScreen(
    container: AppContainer,
    onNavigate: (String) -> Unit
) {
    val viewModel: MoviesViewModel = viewModel(factory = MoviesViewModel.Factory(container))
    val state by viewModel.state.collectAsState()
    val firstFocus = LocalFirstContentFocus.current

    var lastDetailedId by remember { mutableStateOf<Int?>(null) }
    // LazyColumn item index of the row that last triggered a details navigation (-1 = unknown)
    var lastDetailedRowIndex by remember { mutableIntStateOf(-1) }
    val lastDetailedFocusRequester = remember { FocusRequester() }
    val lazyColumnState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED && lastDetailedId != null) {
            // Scroll to the row first so its cards are composed, then request focus
            if (lastDetailedRowIndex >= 0) lazyColumnState.scrollToItem(lastDetailedRowIndex)
            delay(32) // two frames for layout after scroll
            repeat(30) {
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

    // Compute LazyColumn item indices for each row so we can scroll to them on focus restore.
    // Layout: 0=spacer, 1=CW (if present), 2=spacer (if CW), 3=WL (if present), ...
    val cwOffset = if (state.continueWatching.isNotEmpty()) 2 else 0
    val wlOffset = if (state.watchLater.isNotEmpty()) 2 else 0
    val cwRowIndex = 1
    val wlRowIndex = 1 + cwOffset
    val popularRowIndex = 1 + cwOffset + wlOffset
    val topRatedRowIndex = popularRowIndex + 2

    LazyColumn(
        state = lazyColumnState,
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        item { Spacer(Modifier.height(24.dp)) }

        // firstFocus is attached to the first card of whichever row appears first
        val firstRowFocus = Modifier.focusRequester(firstFocus)

        if (state.continueWatching.isNotEmpty()) {
            item {
                ContinueWatchingRow(
                    items = state.continueWatching,
                    firstItemModifier = firstRowFocus,
                    highlightedTmdbId = lastDetailedId?.toString(),
                    highlightedItemFocusRequester = lastDetailedFocusRequester,
                    onWatch = { progress ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = progress.tmdbId,
                            mediaType = "movie",
                            title = progress.title,
                            posterPath = progress.posterPath
                        )
                        onNavigate(AppRoutes.player(StreamUrlBuilder.buildMovieUrl(state.movieProvider, progress.tmdbId.toInt())))
                    },
                    onRestart = { progress ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = progress.tmdbId,
                            mediaType = "movie",
                            title = progress.title,
                            posterPath = progress.posterPath
                        )
                        onNavigate(AppRoutes.player(StreamUrlBuilder.buildMovieUrl(state.movieProvider, progress.tmdbId.toInt())))
                    },
                    onDetails = { progress ->
                        lastDetailedId = progress.tmdbId.toIntOrNull()
                        lastDetailedRowIndex = cwRowIndex
                        onNavigate(AppRoutes.detail("movie", progress.tmdbId.toInt()))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (state.watchLater.isNotEmpty()) {
            item {
                WatchLaterRow(
                    items = state.watchLater,
                    firstItemModifier = if (state.continueWatching.isEmpty()) firstRowFocus else Modifier,
                    highlightedTmdbId = lastDetailedId?.toString(),
                    highlightedItemFocusRequester = lastDetailedFocusRequester,
                    onWatch = { item ->
                        container.nowPlaying.value = NowPlaying(
                            tmdbId = item.tmdbId,
                            mediaType = "movie",
                            title = item.title,
                            posterPath = item.posterPath
                        )
                        onNavigate(AppRoutes.player(StreamUrlBuilder.buildMovieUrl(state.movieProvider, item.tmdbId.toInt())))
                    },
                    onDetails = { item ->
                        lastDetailedId = item.tmdbId.toIntOrNull()
                        lastDetailedRowIndex = wlRowIndex
                        onNavigate(AppRoutes.detail("movie", item.tmdbId.toInt()))
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        item {
            val isFirstRow = state.continueWatching.isEmpty() && state.watchLater.isEmpty()
            MovieRow(
                title = "Popular Movies",
                movies = state.popularMovies,
                firstItemModifier = if (isFirstRow) firstRowFocus else Modifier,
                highlightedItemId = lastDetailedId,
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
                    lastDetailedId = movie.id
                    lastDetailedRowIndex = popularRowIndex
                    onNavigate(AppRoutes.detail("movie", movie.id))
                }
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        item {
            MovieRow(
                title = "Top Rated Movies",
                movies = state.topRatedMovies,
                highlightedItemId = lastDetailedId,
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
                    lastDetailedId = movie.id
                    lastDetailedRowIndex = topRatedRowIndex
                    onNavigate(AppRoutes.detail("movie", movie.id))
                }
            )
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}
