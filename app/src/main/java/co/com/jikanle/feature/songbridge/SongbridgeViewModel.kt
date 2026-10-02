package co.com.jikanle.feature.songbridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.domain.model.TranslatedSongDemo
import co.com.jikanle.core.domain.model.DemoTranslation
import co.com.jikanle.core.domain.repository.TranslatedSongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SongbridgeUiState {
    data object Loading : SongbridgeUiState
    data class Content(val song: TranslatedSongDemo, val translation: DemoTranslation) : SongbridgeUiState
    data object Error : SongbridgeUiState
}

@HiltViewModel
class SongbridgeViewModel @Inject constructor(
    private val repository: TranslatedSongRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SongbridgeUiState>(SongbridgeUiState.Loading)
    val uiState: StateFlow<SongbridgeUiState> = _uiState.asStateFlow()
    private var loadJob: kotlinx.coroutines.Job? = null

    init { load() }

    fun retry() = load()

    fun importStudy(uri: String) = load(uri)

    fun selectLanguage(language: String) {
        val content = _uiState.value as? SongbridgeUiState.Content ?: return
        val translation = content.song.translations.firstOrNull { it.targetLanguage == language } ?: return
        savedState["targetLanguage"] = language
        _uiState.value = content.copy(translation = translation)
    }

    private fun load(uri: String? = null) {
        loadJob?.cancel()
        _uiState.value = SongbridgeUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = (if (uri == null) repository.loadDemoSong() else repository.loadLocalStudy(uri)).fold(
                onSuccess = { song ->
                    val selected: String = savedState["targetLanguage"] ?: "pt"
                    val translation = song.translations.firstOrNull { it.targetLanguage == selected }
                        ?: song.translations.first()
                    SongbridgeUiState.Content(song, translation)
                },
                onFailure = { SongbridgeUiState.Error },
            )
        }
    }
}
