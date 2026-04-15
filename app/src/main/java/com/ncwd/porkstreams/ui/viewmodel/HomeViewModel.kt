package com.ncwd.porkstreams.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.model.Match
import com.ncwd.porkstreams.data.model.Movie
import com.ncwd.porkstreams.data.model.PpvStream
import com.ncwd.porkstreams.data.model.TvShow
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

data class HomeUiState(
    val liveMatches: List<Match> = emptyList(),
    val livePpvStreams: List<PpvStream> = emptyList(),
    val continueWatching: List<WatchProgress> = emptyList(),
    val watchLater: List<WatchLaterItem> = emptyList(),
    val featuredMovie: Movie? = null,
    val trendingMovies: List<Movie> = emptyList(),
    val trendingTvShows: List<TvShow> = emptyList(),
    val movieProvider: String = "vidking",
    val sportsProvider: String = "ppv",
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

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
            .onEach { progress ->
                _state.value = _state.value.copy(continueWatching = progress)
            }
            .launchIn(viewModelScope)
    }

    private fun observeWatchLater() {
        container.watchLaterDao.getAll()
            .onEach { items ->
                _state.value = _state.value.copy(watchLater = items)
            }
            .launchIn(viewModelScope)
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val movieProvider = container.preferencesManager.movieProvider.first()
            val sportsProvider = container.preferencesManager.sportsProvider.first()

            val trendingMoviesDeferred = async { container.moviesRepository.getTrendingMovies() }
            val trendingTvDeferred = async { container.moviesRepository.getTrendingTvShows() }
            val sportsDeferred = async {
                if (sportsProvider == "watchfooty") {
                    container.sportsRepository.getFootyLiveMatches()
                } else null
            }
            val ppvDeferred = async {
                if (sportsProvider == "ppv") {
                    container.sportsRepository.getPpvLiveStreams()
                } else null
            }

            val trendingMovies = trendingMoviesDeferred.await().getOrElse { emptyList() }
            val trendingTv = trendingTvDeferred.await().getOrElse { emptyList() }
            val liveMatches = sportsDeferred.await()?.getOrElse { emptyList() } ?: emptyList()
            val livePpv = ppvDeferred.await()?.getOrElse { emptyList() } ?: emptyList()

            _state.value = _state.value.copy(
                liveMatches = liveMatches,
                livePpvStreams = livePpv,
                featuredMovie = trendingMovies.firstOrNull(),
                trendingMovies = trendingMovies.drop(1),
                trendingTvShows = trendingTv,
                movieProvider = movieProvider,
                sportsProvider = sportsProvider,
                isLoading = false
            )
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(container) as T
        }
    }
}
