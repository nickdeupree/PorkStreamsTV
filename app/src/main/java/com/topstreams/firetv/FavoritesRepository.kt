package com.topstreams.firetv

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoritesRepository(private val context: Context) {
    
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    suspend fun getFavoriteTeams(): List<NbaTeam> = withContext(Dispatchers.IO) {
        val favoriteIds = preferences.getStringSet(KEY_FAVORITES, setOf()) ?: setOf()
        return@withContext NbaTeam.getAllTeams().filter { it.id in favoriteIds }
    }
    
    suspend fun toggleFavorite(teamId: String): Boolean = withContext(Dispatchers.IO) {
        val favorites = preferences.getStringSet(KEY_FAVORITES, setOf())?.toMutableSet() ?: mutableSetOf()
        
        val isFavorite = if (teamId in favorites) {
            favorites.remove(teamId)
            false
        } else {
            favorites.add(teamId)
            true
        }
        
        preferences.edit {
            putStringSet(KEY_FAVORITES, favorites)
        }
        
        return@withContext isFavorite
    }
    
    suspend fun isTeamFavorite(teamId: String): Boolean = withContext(Dispatchers.IO) {
        val favorites = preferences.getStringSet(KEY_FAVORITES, setOf()) ?: setOf()
        return@withContext teamId in favorites
    }
    
    companion object {
        private const val PREF_NAME = "favorites_prefs"
        private const val KEY_FAVORITES = "favorite_teams"
    }
}