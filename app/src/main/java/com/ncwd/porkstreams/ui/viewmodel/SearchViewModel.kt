package com.ncwd.porkstreams.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.model.MultiSearchResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<MultiSearchResult> = emptyList(),
    val movieProvider: String = "vidking",
    val isSearching: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            val provider = container.preferencesManager.movieProvider.first()
            _state.value = _state.value.copy(movieProvider = provider)
        }
        queryFlow
            .debounce(400)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .onEach { q -> search(q) }
            .launchIn(viewModelScope)
    }

    fun setQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        queryFlow.value = query
        if (query.length < 2) _state.value = _state.value.copy(results = emptyList())
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true)
            container.moviesRepository.searchMulti(query)
                .onSuccess { results ->
                    _state.value = _state.value.copy(
                        results = results.filter { it.media_type != "person" },
                        isSearching = false
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(isSearching = false)
                }
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(container) as T
        }
    }
}
