package com.ncwd.porkstreams.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.model.Match
import com.ncwd.porkstreams.data.model.PpvCategory
import com.ncwd.porkstreams.data.model.PpvStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SportsUiState(
    // WatchFooty data
    val liveMatches: List<Match> = emptyList(),
    val upcomingMatches: List<Match> = emptyList(),
    val matchesByLeague: Map<String, List<Match>> = emptyMap(),
    // PPV data
    val ppvCategories: List<PpvCategory> = emptyList(),
    val ppvBasketball: List<PpvStream> = emptyList(),
    val ppvLive: List<PpvStream> = emptyList(),
    val ppvUpcoming: List<PpvStream> = emptyList(),
    // common
    val sportsProvider: String = "watchfooty",
    val isLoading: Boolean = true,
    val error: String? = null
)

class SportsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(SportsUiState())
    val state: StateFlow<SportsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.preferencesManager.sportsProvider.collectLatest { provider ->
                _state.value = _state.value.copy(sportsProvider = provider)
                load(provider)
            }
        }
        viewModelScope.launch {
            container.refreshTrigger.collect { load() }
        }
    }

    fun load(provider: String = _state.value.sportsProvider) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                if (provider == "watchfooty") {
                    loadWatchFooty()
                } else {
                    loadPpv()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadWatchFooty() {
        val liveResult = container.sportsRepository.getFootyLiveMatches()
        val allResult = container.sportsRepository.getFootyAllMatches()

        val liveError = liveResult.exceptionOrNull()
        val allError = allResult.exceptionOrNull()

        // Surface the error if both calls failed
        if (liveResult.isFailure && allResult.isFailure) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "WatchFooty failed: ${allError?.message ?: liveError?.message}"
            )
            return
        }

        val live = liveResult.getOrElse { emptyList() }
        val all = allResult.getOrElse { emptyList() }

        // API returns timestamps in milliseconds
        val now = System.currentTimeMillis()
        val upcoming = all.filter { !it.isLive && it.timestamp > now }.sortedBy { it.timestamp }

        val byLeague = all
            .filter { !it.isEvent }
            .groupBy { leagueDisplayName(it) }
            .mapValues { (_, matches) ->
                matches.sortedWith(compareByDescending<Match> { it.isLive }.thenBy { it.timestamp })
            }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, List<Match>>> { (_, m) -> m.any { it.isLive } }
                    .thenBy { it.key }
            )
            .associate { it.key to it.value }

        _state.value = _state.value.copy(
            liveMatches = live,
            upcomingMatches = upcoming,
            matchesByLeague = byLeague,
            isLoading = false,
            error = if (live.isEmpty() && all.isEmpty())
                "WatchFooty returned no matches (live: ${liveError?.message}, all: ${allError?.message})"
            else null
        )
    }

    private suspend fun loadPpv() {
        val result = container.sportsRepository.getPpvStreams()

        if (result.isFailure) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "PPV failed: ${result.exceptionOrNull()?.message}"
            )
            return
        }

        val categories = result.getOrDefault(emptyList())
        val allStreams = categories.flatMap { it.streams }

        val basketball = categories
            .filter { it.category.equals("Basketball", ignoreCase = true) }
            .flatMap { it.streams }
        val live = allStreams.filter { it.isLive }
        val upcoming = allStreams.filter { it.isUpcoming }.sortedBy { it.starts_at }

        _state.value = _state.value.copy(
            ppvCategories = sortedPpvCategories(categories),
            ppvBasketball = basketball,
            ppvLive = live,
            ppvUpcoming = upcoming,
            isLoading = false,
            error = if (categories.isEmpty()) "PPV returned no categories" else null
        )
    }

    companion object {
        fun leagueDisplayName(match: Match): String {
            val league = match.league?.trim().orEmpty()
            return when {
                match.sport == "basketball" -> classifyBasketball(league)
                league.isNotBlank() -> league
                else -> match.sport.replaceFirstChar { it.uppercase() }
            }
        }

        private fun classifyBasketball(league: String): String {
            val up = league.uppercase()
            return when {
                up.contains("WNBA")                                  -> "WNBA"
                up.contains("G LEAGUE") || up.contains("G-LEAGUE")  -> "NBA G League"
                up.contains("SUMMER LEAGUE")                         -> "NBA Summer League"
                up.contains("NBA")                                   -> "NBA"
                league.isNotBlank()                                  -> "Basketball – $league"
                else                                                 -> "Basketball"
            }
        }

        // Desired display order for PPV category rows
        private val PPV_CATEGORY_ORDER = listOf(
            "basketball", "nba",
            "football", "nfl",
            "baseball", "mlb",
            "hockey", "nhl",
            "soccer",
            "fighting", "mma", "boxing",
            "motorsports", "racing",
            "tennis",
            "wnba",
            "24/7",
            "olympics"
        )

        fun ppvCategoryPriority(category: String): Int {
            val lower = category.lowercase()
            val idx = PPV_CATEGORY_ORDER.indexOfFirst { lower.contains(it) }
            return if (idx == -1) PPV_CATEGORY_ORDER.size else idx
        }

        fun sortedPpvCategories(categories: List<PpvCategory>) =
            categories.sortedBy { ppvCategoryPriority(it.category) }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SportsViewModel(container) as T
        }
    }
}
