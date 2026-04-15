package com.ncwd.porkstreams.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ncwd.porkstreams.data.model.WatchProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchProgressDao {

    @Query("SELECT * FROM watch_progress ORDER BY lastUpdated DESC")
    fun getAllProgress(): Flow<List<WatchProgress>>

    @Query("SELECT * FROM watch_progress WHERE tmdbId = :id LIMIT 1")
    suspend fun getById(id: String): WatchProgress?

    @Upsert
    suspend fun upsert(progress: WatchProgress)

    @Query("DELETE FROM watch_progress WHERE tmdbId = :id")
    suspend fun deleteById(id: String)
}
