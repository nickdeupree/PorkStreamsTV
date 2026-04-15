package com.ncwd.porkstreams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.ui.focus.focusRequester
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.LocalFirstContentFocus
import com.ncwd.porkstreams.ui.components.PpvStreamRow
import com.ncwd.porkstreams.ui.components.SportMatchRow
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.Background
import com.ncwd.porkstreams.ui.theme.TextSecondary
import com.ncwd.porkstreams.ui.viewmodel.SportsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SportsScreen(
    container: AppContainer,
    onNavigate: (String) -> Unit
) {
    val viewModel: SportsViewModel = viewModel(factory = SportsViewModel.Factory(container))
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentRed)
        }
        return
    }

    val error = state.error
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background).padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = error,
                color = AccentRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val isWatchFooty = state.sportsProvider == "watchfooty"
    val firstFocus = LocalFirstContentFocus.current
    val firstItemMod = Modifier.focusRequester(firstFocus)

    // Compute derived lists outside the LazyColumn so we can determine which row is first
    val nonBasketballLive = state.ppvLive.filter {
        !it.category_name.equals("Basketball", ignoreCase = true)
    }

    // Which row appears first in the PPV layout?
    val ppvFirstRow = when {
        nonBasketballLive.isNotEmpty() -> "live"
        state.ppvUpcoming.isNotEmpty() -> "upcoming"
        state.ppvBasketball.isNotEmpty() -> "basketball"
        else -> "category"
    }
    // Which row appears first in the WatchFooty layout?
    val footyFirstRow = when {
        state.liveMatches.isNotEmpty() -> "live"
        state.upcomingMatches.isNotEmpty() -> "upcoming"
        else -> "league"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        item { Spacer(Modifier.height(16.dp)) }

        if (isWatchFooty) {
            if (state.liveMatches.isNotEmpty()) {
                item {
                    SportMatchRow(
                        title = "Live Now",
                        matches = state.liveMatches,
                        firstItemModifier = if (footyFirstRow == "live") firstItemMod else Modifier,
                        onClick = { match ->
                            val url = match.firstStreamUrl ?: return@SportMatchRow
                            onNavigate(AppRoutes.player(url))
                        }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            if (state.upcomingMatches.isNotEmpty()) {
                item {
                    SportMatchRow(
                        title = "Upcoming",
                        matches = state.upcomingMatches.take(20),
                        firstItemModifier = if (footyFirstRow == "upcoming") firstItemMod else Modifier,
                        onClick = { /* upcoming — no stream */ }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            state.matchesByLeague.entries.forEachIndexed { leagueIndex, (leagueName, matches) ->
                item {
                    SportMatchRow(
                        title = leagueName,
                        matches = matches.take(20),
                        firstItemModifier = if (footyFirstRow == "league" && leagueIndex == 0) firstItemMod else Modifier,
                        onClick = { match ->
                            val url = match.firstStreamUrl ?: return@SportMatchRow
                            onNavigate(AppRoutes.player(url))
                        }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        } else {
            // PPV — order: Live Now, Upcoming, NBA Basketball, rest
            if (nonBasketballLive.isNotEmpty()) {
                item {
                    PpvStreamRow(
                        title = "Live Now",
                        streams = nonBasketballLive,
                        firstItemModifier = if (ppvFirstRow == "live") firstItemMod else Modifier,
                        onClick = { stream ->
                            val url = stream.iframe ?: return@PpvStreamRow
                            onNavigate(AppRoutes.player(url))
                        }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            if (state.ppvUpcoming.isNotEmpty()) {
                item {
                    PpvStreamRow(
                        title = "Upcoming",
                        streams = state.ppvUpcoming.take(20),
                        firstItemModifier = if (ppvFirstRow == "upcoming") firstItemMod else Modifier,
                        onClick = { }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            if (state.ppvBasketball.isNotEmpty()) {
                item {
                    PpvStreamRow(
                        title = "NBA Basketball",
                        streams = state.ppvBasketball,
                        firstItemModifier = if (ppvFirstRow == "basketball") firstItemMod else Modifier,
                        onClick = { stream ->
                            val url = stream.iframe ?: return@PpvStreamRow
                            onNavigate(AppRoutes.player(url))
                        }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            state.ppvCategories
                .filter { !it.category.equals("Basketball", ignoreCase = true) }
                .forEachIndexed { catIndex, category ->
                    if (category.streams.isNotEmpty()) {
                        item {
                            PpvStreamRow(
                                title = category.category,
                                streams = category.streams.take(10),
                                firstItemModifier = if (ppvFirstRow == "category" && catIndex == 0) firstItemMod else Modifier,
                                onClick = { stream ->
                                    val url = stream.iframe ?: return@PpvStreamRow
                                    onNavigate(AppRoutes.player(url))
                                }
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
        }

        val hasContent = state.liveMatches.isNotEmpty() ||
            state.matchesByLeague.isNotEmpty() ||
            state.ppvBasketball.isNotEmpty() ||
            state.ppvLive.isNotEmpty() ||
            state.ppvUpcoming.isNotEmpty() ||
            state.ppvCategories.any { it.streams.isNotEmpty() }
        if (!hasContent) {
            item {
                Box(
                    Modifier.fillMaxSize().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No sports content available right now.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}
