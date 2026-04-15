package com.ncwd.porkstreams.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.model.CastMember
import com.ncwd.porkstreams.data.model.MovieDetails
import com.ncwd.porkstreams.data.model.TvEpisode
import com.ncwd.porkstreams.data.model.TvSeason
import com.ncwd.porkstreams.data.model.WatchLaterItem
import com.ncwd.porkstreams.data.model.WatchProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DetailUiState(
    val details: MovieDetails? = null,
    val cast: List<CastMember> = emptyList(),
    val seasons: List<TvSeason> = emptyList(),
    val selectedSeason: Int = 1,
    val selectedEpisode: Int = 1,
    val episodes: List<TvEpisode> = emptyList(),
    val watchProgress: WatchProgress? = null,
    val isInWatchlist: Boolean = false,
    val movieProvider: String = "vidsrc",
    val isLoading: Boolean = true,
    val error: String? = null
)

class DetailViewModel(
    private val container: AppContainer,
    private val mediaType: String,
    private val id: Int
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val provider = container.preferencesManager.movieProvider.first()

            val progress = container.watchProgressDao.getById(id.toString())
            val inWatchlist = container.watchLaterDao.exists(id.toString())

            if (mediaType == "movie") {
                val details = container.moviesRepository.getMovieDetails(id).getOrNull()
                val cast = container.moviesRepository.getMovieCredits(id).getOrNull()?.cast?.take(15) ?: emptyList()
                _state.value = _state.value.copy(
                    details = details, cast = cast, watchProgress = progress,
                    isInWatchlist = inWatchlist, movieProvider = provider, isLoading = false
                )
            } else {
                val details = container.moviesRepository.getTvDetails(id).getOrNull()
                val cast = container.moviesRepository.getTvCredits(id).getOrNull()?.cast?.take(15) ?: emptyList()
                val numSeasons = details?.number_of_seasons ?: 1
                val seasons = (1..numSeasons).mapNotNull { season ->
                    container.moviesRepository.getTvSeason(id, season).getOrNull()
                }
                val startSeason = progress?.season ?: 1
                val startEpisode = progress?.episode ?: 1
                val episodes = seasons.firstOrNull { it.season_number == startSeason }?.episodes ?: emptyList()
                _state.value = _state.value.copy(
                    details = details, cast = cast, seasons = seasons, episodes = episodes,
                    selectedSeason = startSeason, selectedEpisode = startEpisode,
                    watchProgress = progress, isInWatchlist = inWatchlist,
                    movieProvider = provider, isLoading = false
                )
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        viewModelScope.launch {
            val season = _state.value.seasons.firstOrNull { it.season_number == seasonNumber }
                ?: container.moviesRepository.getTvSeason(id, seasonNumber).getOrNull()
                ?: return@launch
            _state.value = _state.value.copy(
                selectedSeason = seasonNumber,
                selectedEpisode = 1,
                episodes = season.episodes ?: emptyList()
            )
        }
    }

    fun selectEpisode(episodeNumber: Int) {
        _state.value = _state.value.copy(selectedEpisode = episodeNumber)
    }

    suspend fun toggleWatchlist() {
        val details = _state.value.details ?: return
        if (_state.value.isInWatchlist) {
            container.watchLaterDao.deleteById(id.toString())
            _state.value = _state.value.copy(isInWatchlist = false)
        } else {
            container.watchLaterDao.upsert(
                WatchLaterItem(
                    tmdbId = id.toString(),
                    type = mediaType,
                    title = details.displayTitle,
                    posterPath = details.poster_path
                )
            )
            _state.value = _state.value.copy(isInWatchlist = true)
        }
    }

    class Factory(
        private val container: AppContainer,
        private val mediaType: String,
        private val id: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(container, mediaType, id) as T
        }
    }
}
