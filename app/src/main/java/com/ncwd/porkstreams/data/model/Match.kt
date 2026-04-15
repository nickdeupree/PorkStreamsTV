package com.ncwd.porkstreams.data.model

import com.squareup.moshi.JsonClass

private const val WATCHFOOTY_BASE = "https://api.watchfooty.st"

@JsonClass(generateAdapter = true)
data class Match(
    val matchId: String,
    val title: String,
    val poster: String? = null,
    val teams: Teams = Teams(),
    val scores: Scores? = null,
    val status: String? = null,
    val currentMinute: String? = null,
    val currentMinuteNumber: Int? = null,
    val date: String,
    val timestamp: Long,
    val league: String? = null,
    val leagueLogo: String? = null,
    val leagueLogoId: String? = null,
    val sport: String,
    val isEvent: Boolean = false,
    val streams: List<MatchStream> = emptyList()
) {
    val isLive: Boolean get() = status == "in" || status == "live"
    val posterUrl: String? get() = poster?.let {
        if (it.startsWith("http")) it else "$WATCHFOOTY_BASE$it"
    }
    val leagueLogoUrl: String? get() = leagueLogo?.let {
        if (it.startsWith("http")) it else "$WATCHFOOTY_BASE$it"
    }
    val firstStreamUrl: String? get() = streams.firstOrNull { !it.nsfw }?.url
}

@JsonClass(generateAdapter = true)
data class Teams(
    val home: TeamInfo = TeamInfo(),
    val away: TeamInfo = TeamInfo()
)

@JsonClass(generateAdapter = true)
data class TeamInfo(
    val name: String = "TBD",
    val logoUrl: String? = null,
    val logoId: String? = null
) {
    val logoFullUrl: String? get() = logoUrl?.let {
        if (it.startsWith("http")) it else "$WATCHFOOTY_BASE$it"
    }
}

@JsonClass(generateAdapter = true)
data class Scores(
    val home: String? = null,
    val away: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchStream(
    val id: String,
    val url: String,
    val quality: String? = null,
    val language: String? = null,
    val isRedirect: Boolean = false,
    val nsfw: Boolean = false,
    val ads: Boolean = false
)

@JsonClass(generateAdapter = true)
data class Sport(
    val name: String,
    val displayName: String
)
