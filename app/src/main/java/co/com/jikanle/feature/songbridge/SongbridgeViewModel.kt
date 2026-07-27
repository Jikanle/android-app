package co.com.jikanle.feature.songbridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.domain.model.TranslatedSongDemo
import co.com.jikanle.core.domain.repository.TranslatedSongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SongbridgeUiState {
    data object Loading : SongbridgeUiState
    data class Content(val song: TranslatedSongDemo) : SongbridgeUiState
    data object Error : SongbridgeUiState
}

@HiltViewModel
class SongbridgeViewModel @Inject constructor(
    private val repository: TranslatedSongRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SongbridgeUiState>(SongbridgeUiState.Loading)
    val uiState: StateFlow<SongbridgeUiState> = _uiState.asStateFlow()

    init { load() }

    fun retry() = load()

    private fun load() {
        _uiState.value = SongbridgeUiState.Loading
        viewModelScope.launch {
            _uiState.value = repository.loadDemoSong().fold(
                onSuccess = SongbridgeUiState::Content,
                onFailure = { SongbridgeUiState.Error },
            )
        }
    }
}
