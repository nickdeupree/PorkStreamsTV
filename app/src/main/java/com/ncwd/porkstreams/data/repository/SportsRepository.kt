package com.ncwd.porkstreams.data.repository

import com.ncwd.porkstreams.data.api.FootyApi
import com.ncwd.porkstreams.data.api.PpvApi
import com.ncwd.porkstreams.data.model.Match
import com.ncwd.porkstreams.data.model.PpvCategory
import com.ncwd.porkstreams.data.model.PpvResponse
import com.ncwd.porkstreams.data.model.PpvStream
import com.ncwd.porkstreams.data.model.Sport

class SportsRepository(
    private val footyApi: FootyApi,
    private val ppvApis: List<PpvApi>
) {
    // --- WatchFooty ---

    suspend fun getFootySports(): Result<List<Sport>> = runCatching {
        footyApi.getSports()
    }

    suspend fun getFootyNbaMatches(): Result<List<Match>> = runCatching {
        footyApi.getMatchesBySport("basketball")
    }

    suspend fun getFootyMatchesBySport(sport: String): Result<List<Match>> = runCatching {
        footyApi.getMatchesBySport(sport)
    }

    suspend fun getFootyLiveMatches(): Result<List<Match>> = runCatching {
        footyApi.getAllLiveMatches()
    }

    suspend fun getFootyAllMatches(): Result<List<Match>> = runCatching {
        footyApi.getAllMatches()
    }

    // --- PPV (tries each base URL in order, returns first success) ---

    private suspend fun fetchPpvResponse(): PpvResponse {
        var lastError: Throwable? = null
        for (api in ppvApis) {
            try {
                return api.getStreams()
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: Exception("All PPV sources failed")
    }

    suspend fun getPpvStreams(): Result<List<PpvCategory>> = runCatching {
        fetchPpvResponse().streams
    }

    suspend fun getPpvBasketball(): Result<List<PpvStream>> = runCatching {
        fetchPpvResponse().streams
            .filter { it.category.equals("Basketball", ignoreCase = true) }
            .flatMap { it.streams }
    }

    suspend fun getPpvLiveStreams(): Result<List<PpvStream>> = runCatching {
        fetchPpvResponse().streams
            .flatMap { it.streams }
            .filter { it.isLive }
    }

    suspend fun getPpvUpcomingStreams(): Result<List<PpvStream>> = runCatching {
        fetchPpvResponse().streams
            .flatMap { it.streams }
            .filter { it.isUpcoming }
            .sortedBy { it.starts_at }
    }
}
