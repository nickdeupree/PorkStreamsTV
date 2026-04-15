package com.ncwd.porkstreams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.providers.StreamUrlBuilder
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.LocalFirstContentFocus
import com.ncwd.porkstreams.ui.components.MovieCard
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.Background
import com.ncwd.porkstreams.ui.theme.CardBackground
import com.ncwd.porkstreams.ui.theme.TextSecondary
import com.ncwd.porkstreams.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    container: AppContainer,
    onNavigate: (String) -> Unit
) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(container))
    val state by viewModel.state.collectAsState()
    val firstFocus = LocalFirstContentFocus.current
    val firstGridItemFocus = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            placeholder = { androidx.compose.material3.Text("Search movies and TV shows...", color = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = CardBackground,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(firstFocus)
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.DirectionDown &&
                        event.type == KeyEventType.KeyDown &&
                        state.results.isNotEmpty()
                    ) {
                        try { firstGridItemFocus.requestFocus() } catch (_: Exception) {}
                        true
                    } else false
                }
                .padding(horizontal = 48.dp, vertical = 16.dp)
        )

        when {
            state.isSearching -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }
            state.query.length >= 2 && state.results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results for \"${state.query}\"", color = TextSecondary)
                }
            }
            state.results.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.results, key = { "${it.media_type}_${it.id}" }) { result ->
                        val isFirst = result == state.results.first()
                        MovieCard(
                            title = result.displayTitle,
                            posterUrl = result.posterUrl,
                            rating = result.vote_average,
                            modifier = if (isFirst) Modifier.focusRequester(firstGridItemFocus) else Modifier,
                            onWatch = {
                                val url = if (result.media_type == "movie") {
                                    StreamUrlBuilder.buildMovieUrl(state.movieProvider, result.id)
                                } else {
                                    StreamUrlBuilder.buildTvUrl(state.movieProvider, result.id, 1, 1)
                                }
                                onNavigate(AppRoutes.player(url))
                            },
                            onDetails = {
                                onNavigate(AppRoutes.detail(result.media_type, result.id))
                            }
                        )
                    }
                }
            }
            else -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Search for movies and TV shows",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
