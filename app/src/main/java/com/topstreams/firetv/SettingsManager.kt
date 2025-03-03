package com.topstreams.firetv

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for Context to easily access DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    
    // Keys for preferences
    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val USE_WEBVIEW_KEY = booleanPreferencesKey("use_webview")
    }
    
    // Get the current theme preference (default is dark)
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: true // Default to dark theme
    }
    
    // Get the WebViewer preference (default is false - use normal video player)
    val useWebViewer: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_WEBVIEW_KEY] ?: false // Default to normal player
    }
    
    // Toggle theme preference
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val currentTheme = preferences[DARK_THEME_KEY] ?: true
            preferences[DARK_THEME_KEY] = !currentTheme
        }
    }
    
    // Toggle WebViewer preference
    suspend fun toggleWebViewer() {
        context.dataStore.edit { preferences ->
            val currentWebViewerSetting = preferences[USE_WEBVIEW_KEY] ?: false
            preferences[USE_WEBVIEW_KEY] = !currentWebViewerSetting
        }
    }
    
    // Set a specific theme
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }
    
    // Set a specific WebViewer preference
    suspend fun setUseWebViewer(useWebView: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_WEBVIEW_KEY] = useWebView
        }
    }
}