package com.ncwd.porkstreams.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val MOVIE_PROVIDER = stringPreferencesKey("movie_provider")
        val SPORTS_PROVIDER = stringPreferencesKey("sports_provider")
    }

    val movieProvider: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[MOVIE_PROVIDER] ?: "vidking"
    }

    val sportsProvider: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SPORTS_PROVIDER] ?: "ppv"
    }

    suspend fun setMovieProvider(provider: String) {
        context.dataStore.edit { prefs ->
            prefs[MOVIE_PROVIDER] = provider
        }
    }

    suspend fun setSportsProvider(provider: String) {
        context.dataStore.edit { prefs ->
            prefs[SPORTS_PROVIDER] = provider
        }
    }
}
