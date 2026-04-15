package com.ncwd.porkstreams.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.model.IptvChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IptvUiState(
    val channels: List<IptvChannel> = emptyList(),
    val groups: List<String> = emptyList(),
    val selectedGroup: String = "All",
    val filteredChannels: List<IptvChannel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class IptvViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(IptvUiState())
    val state: StateFlow<IptvUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            container.refreshTrigger.collect { load() }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            container.iptvRepository.getChannels()
                .onSuccess { channels ->
                    val groups = listOf("All") + channels.mapNotNull { it.group }.distinct().sorted()
                    _state.value = _state.value.copy(
                        channels = channels,
                        groups = groups,
                        filteredChannels = channels,
                        isLoading = false
                    )
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = err.message ?: "Failed to load channels"
                    )
                }
        }
    }

    fun selectGroup(group: String) {
        val channels = _state.value.channels
        val filtered = if (group == "All") channels else channels.filter { it.group == group }
        _state.value = _state.value.copy(selectedGroup = group, filteredChannels = filtered)
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return IptvViewModel(container) as T
        }
    }
}
