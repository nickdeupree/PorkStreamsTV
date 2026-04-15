package com.ncwd.porkstreams.data.providers

object StreamUrlBuilder {

    fun buildMovieUrl(provider: String, tmdbId: Int): String = when (provider) {
        "vidsrc" -> "https://www.vidsrc.wtf/embed/movie/$tmdbId"
        "vidking" -> "https://www.vidking.net/embed/movie/$tmdbId"
        "rive" -> "https://rivestream.org/embed?type=movie&id=$tmdbId"
        "superembed" -> "https://multiembed.mov/directstream.php?video_id=$tmdbId&tmdb=1"
        "2embed" -> "https://api.2embed.cc/embed/$tmdbId"
        else -> "https://www.vidsrc.wtf/embed/movie/$tmdbId"
    }

    fun buildTvUrl(provider: String, tmdbId: Int, season: Int, episode: Int): String = when (provider) {
        "vidsrc" -> "https://www.vidsrc.wtf/embed/tv/$tmdbId/$season/$episode"
        "vidking" -> "https://www.vidking.net/embed/tv/$tmdbId/$season/$episode"
        "rive" -> "https://rivestream.org/embed?type=tv&id=$tmdbId&season=$season&episode=$episode"
        "superembed" -> "https://multiembed.mov/directstream.php?video_id=$tmdbId&tmdb=1&s=$season&e=$episode"
        "2embed" -> "https://api.2embed.cc/embedtv/$tmdbId&s=$season&e=$episode"
        else -> "https://www.vidsrc.wtf/embed/tv/$tmdbId/$season/$episode"
    }

    val movieProviders = listOf(
        ProviderInfo("vidsrc", "VidSrc"),
        ProviderInfo("vidking", "VidKing"),
        ProviderInfo("rive", "RiveStream"),
        ProviderInfo("superembed", "SuperEmbed"),
        ProviderInfo("2embed", "2Embed")
    )

    val sportsProviders = listOf(
        ProviderInfo("watchfooty", "WatchFooty"),
        ProviderInfo("ppv", "PPV")
    )
}

data class ProviderInfo(val id: String, val displayName: String)
