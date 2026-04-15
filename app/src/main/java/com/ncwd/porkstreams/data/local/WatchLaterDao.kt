package com.ncwd.porkstreams.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ncwd.porkstreams.data.model.WatchLaterItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchLaterDao {

    @Query("SELECT * FROM watch_later ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchLaterItem>>

    @Query("SELECT * FROM watch_later WHERE type = :type ORDER BY addedAt DESC")
    fun getByType(type: String): Flow<List<WatchLaterItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM watch_later WHERE tmdbId = :tmdbId)")
    suspend fun exists(tmdbId: String): Boolean

    @Upsert
    suspend fun upsert(item: WatchLaterItem)

    @Query("DELETE FROM watch_later WHERE tmdbId = :tmdbId")
    suspend fun deleteById(tmdbId: String)
}
