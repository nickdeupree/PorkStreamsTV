package com.ncwd.porkstreams.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ncwd.porkstreams.data.model.Match
import com.ncwd.porkstreams.data.model.Movie
import com.ncwd.porkstreams.data.model.PpvStream
import com.ncwd.porkstreams.data.model.TvShow
import com.ncwd.porkstreams.data.model.WatchLaterItem
import com.ncwd.porkstreams.data.model.WatchProgress

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    highlightedItemId: Int? = null,
    highlightedItemFocusRequester: FocusRequester? = null,
    onWatch: (Movie) -> Unit = {},
    onDetails: (Movie) -> Unit = {}
) {
    if (movies.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            content = {
                itemsIndexed(movies, key = { _, it -> it.id }) { index, movie ->
                    val baseMod = if (index == 0) firstItemModifier else Modifier
                    val highlightMod = if (highlightedItemId != null && movie.id == highlightedItemId && highlightedItemFocusRequester != null)
                        Modifier.focusRequester(highlightedItemFocusRequester) else Modifier
                    MovieCard(
                        title = movie.displayTitle,
                        posterUrl = movie.posterUrl,
                        rating = movie.vote_average,
                        modifier = baseMod.then(highlightMod).padding(end = 12.dp),
                        onWatch = { onWatch(movie) },
                        onDetails = { onDetails(movie) }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvShowRow(
    title: String,
    shows: List<TvShow>,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    highlightedItemId: Int? = null,
    highlightedItemFocusRequester: FocusRequester? = null,
    onWatch: (TvShow) -> Unit = {},
    onDetails: (TvShow) -> Unit = {}
) {
    if (shows.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            content = {
                itemsIndexed(shows, key = { _, it -> it.id }) { index, show ->
                    val baseMod = if (index == 0) firstItemModifier else Modifier
                    val highlightMod = if (highlightedItemId != null && show.id == highlightedItemId && highlightedItemFocusRequester != null)
                        Modifier.focusRequester(highlightedItemFocusRequester) else Modifier
                    MovieCard(
                        title = show.name,
                        posterUrl = show.posterUrl,
                        rating = show.vote_average,
                        modifier = baseMod.then(highlightMod).padding(end = 12.dp),
                        onWatch = { onWatch(show) },
                        onDetails = { onDetails(show) }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContinueWatchingRow(
    items: List<WatchProgress>,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    highlightedTmdbId: String? = null,
    highlightedItemFocusRequester: FocusRequester? = null,
    onWatch: (WatchProgress) -> Unit = {},
    onRestart: (WatchProgress) -> Unit = {},
    onDetails: (WatchProgress) -> Unit = {}
) {
    if (items.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = "Continue Watching",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            content = {
                itemsIndexed(items, key = { _, it -> it.tmdbId }) { index, progress ->
                    val baseMod = if (index == 0) firstItemModifier else Modifier
                    val highlightMod = if (highlightedTmdbId != null && progress.tmdbId == highlightedTmdbId && highlightedItemFocusRequester != null)
                        Modifier.focusRequester(highlightedItemFocusRequester) else Modifier
                    ContinueWatchingCard(
                        title = progress.title,
                        posterUrl = progress.posterUrl,
                        progress = progress.progressPercent,
                        modifier = baseMod.then(highlightMod).padding(end = 12.dp),
                        onWatch = { onWatch(progress) },
                        onRestart = { onRestart(progress) },
                        onDetails = { onDetails(progress) }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WatchLaterRow(
    items: List<WatchLaterItem>,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    highlightedTmdbId: String? = null,
    highlightedItemFocusRequester: FocusRequester? = null,
    onWatch: (WatchLaterItem) -> Unit = {},
    onDetails: (WatchLaterItem) -> Unit = {}
) {
    if (items.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = "Watch Later",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            content = {
                itemsIndexed(items, key = { _, it -> it.tmdbId }) { index, item ->
                    val baseMod = if (index == 0) firstItemModifier else Modifier
                    val highlightMod = if (highlightedTmdbId != null && item.tmdbId == highlightedTmdbId && highlightedItemFocusRequester != null)
                        Modifier.focusRequester(highlightedItemFocusRequester) else Modifier
                    MovieCard(
                        title = item.title,
                        posterUrl = item.posterUrl,
                        rating = null,
                        modifier = baseMod.then(highlightMod).padding(end = 12.dp),
                        onWatch = { onWatch(item) },
                        onDetails = { onDetails(item) }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SportMatchRow(
    title: String,
    matches: List<Match>,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    onClick: (Match) -> Unit = {}
) {
    if (matches.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            content = {
                itemsIndexed(matches, key = { _, it -> it.matchId }) { index, match ->
                    SportMatchCard(
                        match = match,
                        modifier = (if (index == 0) firstItemModifier else Modifier).padding(end = 12.dp),
                        onClick = { onClick(match) }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PpvStreamRow(
    title: String,
    streams: List<PpvStream>,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    onClick: (PpvStream) -> Unit = {}
) {
    if (streams.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            content = {
                itemsIndexed(streams, key = { _, it -> it.id }) { index, stream ->
                    PpvStreamCard(
                        stream = stream,
                        modifier = (if (index == 0) firstItemModifier else Modifier).padding(end = 12.dp),
                        onClick = { onClick(stream) }
                    )
                }
            }
        )
    }
}
