package com.ncwd.porkstreams.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Movie(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val overview: String? = null,
    val vote_average: Double? = null,
    val release_date: String? = null,
    val first_air_date: String? = null,
    val media_type: String? = null,
    val genre_ids: List<Int>? = null
) {
    val displayTitle: String get() = title ?: name ?: "Unknown"
    val displayDate: String get() = release_date ?: first_air_date ?: ""
    val posterUrl: String? get() = poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
    val backdropUrl: String? get() = backdrop_path?.let { "https://image.tmdb.org/t/p/w1280$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbPagedResponse<T>(
    val results: List<T>,
    val page: Int = 1,
    val total_pages: Int = 1
)

@JsonClass(generateAdapter = true)
data class TvShow(
    val id: Int,
    val name: String,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val overview: String? = null,
    val vote_average: Double? = null,
    val number_of_seasons: Int? = null,
    val number_of_episodes: Int? = null,
    val first_air_date: String? = null,
    val genre_ids: List<Int>? = null
) {
    val posterUrl: String? get() = poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
    val backdropUrl: String? get() = backdrop_path?.let { "https://image.tmdb.org/t/p/w1280$it" }
}

@JsonClass(generateAdapter = true)
data class TvSeason(
    val season_number: Int,
    val episode_count: Int = 0,
    val episodes: List<TvEpisode>? = null
)

@JsonClass(generateAdapter = true)
data class TvEpisode(
    val id: Int,
    val episode_number: Int,
    val name: String,
    val still_path: String? = null,
    val overview: String? = null
) {
    val stillUrl: String? get() = still_path?.let { "https://image.tmdb.org/t/p/w500$it" }
}

@JsonClass(generateAdapter = true)
data class MovieDetails(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val overview: String? = null,
    val vote_average: Double? = null,
    val release_date: String? = null,
    val first_air_date: String? = null,
    val runtime: Int? = null,
    val genres: List<Genre>? = null,
    val number_of_seasons: Int? = null,
    val number_of_episodes: Int? = null
) {
    val displayTitle: String get() = title ?: name ?: "Unknown"
    val posterUrl: String? get() = poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
    val backdropUrl: String? get() = backdrop_path?.let { "https://image.tmdb.org/t/p/w1280$it" }
    val genreString: String get() = genres?.joinToString(", ") { it.name } ?: ""
}

@JsonClass(generateAdapter = true)
data class Genre(val id: Int, val name: String)

@JsonClass(generateAdapter = true)
data class CreditsResponse(
    val id: Int,
    val cast: List<CastMember>
)

@JsonClass(generateAdapter = true)
data class CastMember(
    val id: Int,
    val name: String,
    val character: String? = null,
    val profile_path: String? = null
) {
    val profileUrl: String? get() = profile_path?.let { "https://image.tmdb.org/t/p/w185$it" }
}

@JsonClass(generateAdapter = true)
data class MultiSearchResult(
    val id: Int,
    val media_type: String,
    val title: String? = null,
    val name: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val overview: String? = null,
    val vote_average: Double? = null,
    val release_date: String? = null,
    val first_air_date: String? = null
) {
    val displayTitle: String get() = title ?: name ?: "Unknown"
    val posterUrl: String? get() = poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
}
