package com.ncwd.porkstreams.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.model.Movie
import com.ncwd.porkstreams.data.model.WatchLaterItem
import com.ncwd.porkstreams.data.model.WatchProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MoviesUiState(
    val popularMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val continueWatching: List<WatchProgress> = emptyList(),
    val watchLater: List<WatchLaterItem> = emptyList(),
    val movieProvider: String = "vidsrc",
    val isLoading: Boolean = true,
    val error: String? = null
)

class MoviesViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(MoviesUiState())
    val state: StateFlow<MoviesUiState> = _state.asStateFlow()

    init {
        observeWatchProgress()
        observeWatchLater()
        load()
        viewModelScope.launch {
            container.refreshTrigger.collect { load() }
        }
    }

    private fun observeWatchProgress() {
        container.watchProgressDao.getAllProgress()
            .onEach { all ->
                _state.value = _state.value.copy(continueWatching = all.filter { it.type == "movie" })
            }
            .launchIn(viewModelScope)
    }

    private fun observeWatchLater() {
        container.watchLaterDao.getByType("movie")
            .onEach { items ->
                _state.value = _state.value.copy(watchLater = items)
            }
            .launchIn(viewModelScope)
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val provider = container.preferencesManager.movieProvider.first()
            val popularDeferred = async { container.moviesRepository.getPopularMovies() }
            val topRatedDeferred = async { container.moviesRepository.getTopRatedMovies() }

            val popular = popularDeferred.await().getOrElse { emptyList() }
            val topRated = topRatedDeferred.await().getOrElse { emptyList() }

            _state.value = _state.value.copy(
                popularMovies = popular,
                topRatedMovies = topRated,
                movieProvider = provider,
                isLoading = false
            )
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MoviesViewModel(container) as T
        }
    }
}
