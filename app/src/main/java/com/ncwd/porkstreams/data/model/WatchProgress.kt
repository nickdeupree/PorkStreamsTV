package com.ncwd.porkstreams.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_progress")
data class WatchProgress(
    @PrimaryKey val tmdbId: String,
    val type: String,           // "movie" or "tv"
    val title: String,
    val posterPath: String? = null,
    val progressSeconds: Float = 0f,
    val durationSeconds: Float = 0f,
    val season: Int? = null,
    val episode: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (durationSeconds > 0) progressSeconds / durationSeconds else 0f
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}
