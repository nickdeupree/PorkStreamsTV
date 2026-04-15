package com.ncwd.porkstreams.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ncwd.porkstreams.data.local.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsUiState(
    val movieProvider: String = "vidsrc",
    val sportsProvider: String = "watchfooty"
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        combine(
            container.preferencesManager.movieProvider,
            container.preferencesManager.sportsProvider
        ) { movie, sports -> SettingsUiState(movie, sports) }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun setMovieProvider(provider: String) {
        viewModelScope.launch { container.preferencesManager.setMovieProvider(provider) }
    }

    fun setSportsProvider(provider: String) {
        viewModelScope.launch { container.preferencesManager.setSportsProvider(provider) }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(container) as T
        }
    }
}
