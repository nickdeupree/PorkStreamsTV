package com.ncwd.porkstreams.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_later")
data class WatchLaterItem(
    @PrimaryKey val tmdbId: String,
    val type: String,           // "movie" or "tv"
    val title: String,
    val posterPath: String? = null,
    val addedAt: Long = System.currentTimeMillis()
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}
