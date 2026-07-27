package co.com.jikanle.feature.songbridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.jikanle.R
import co.com.jikanle.core.design.theme.JikanleTypography
import co.com.jikanle.core.domain.model.DemoLyricLine
import co.com.jikanle.core.domain.model.DemoTranslation
import co.com.jikanle.core.domain.model.DemoVocabularyItem
import co.com.jikanle.core.domain.model.TranslatedSongDemo

@Composable
fun SongbridgeScreen(viewModel: SongbridgeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        SongbridgeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        SongbridgeUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.songbridge_error), style = JikanleTypography.body)
                TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.retry)) }
            }
        }
        is SongbridgeUiState.Content -> SongbridgeContent(state.song)
    }
}

@Composable
private fun SongbridgeContent(song: TranslatedSongDemo) {
    var targetLanguage by remember { mutableStateOf("es") }
    val translation = song.translations.firstOrNull { it.targetLanguage == targetLanguage }
        ?: song.translations.firstOrNull()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
        item { DemoHeader(song) }
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                AssistChip(onClick = {}, label = { Text(stringResource(R.string.local_demo_source)) })
                Text(
                    text = stringResource(R.string.supabase_demo_todo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            LanguageSelector(
                selected = translation?.targetLanguage.orEmpty(),
                translations = song.translations,
                onSelect = { targetLanguage = it },
            )
        }
        item { SectionTitle(stringResource(R.string.lyrics_label), stringResource(R.string.lyrics_title)) }
        if (translation != null) {
            val translatedByIndex = translation.lines.associateBy { it.lineIndex }
            items(song.lyrics, key = DemoLyricLine::lineIndex) { original ->
                LyricRow(original, translatedByIndex[original.lineIndex]?.text.orEmpty())
            }
            translation.alignmentNote?.let { note -> item { AlignmentNote(note) } }
        }
        item { SectionTitle(stringResource(R.string.vocabulary_label), stringResource(R.string.vocabulary_title)) }
        items(song.vocabulary, key = DemoVocabularyItem::term) { VocabularyCard(it) }
    }
}

@Composable
private fun DemoHeader(song: TranslatedSongDemo) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp)) {
        Text(stringResource(R.string.jikanle_wordmark), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(28.dp))
        Text(song.titleOriginal, style = JikanleTypography.display)
        song.titleRomanized?.let {
            Text(it, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)
        }
        Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.source_language, song.sourceLanguage.uppercase()), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LanguageSelector(selected: String, translations: List<DemoTranslation>, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        translations.forEach { translation ->
            FilterChip(
                selected = selected == translation.targetLanguage,
                onClick = { onSelect(translation.targetLanguage) },
                label = { Text(translation.targetLanguage.uppercase()) },
            )
        }
    }
}

@Composable
private fun SectionTitle(label: String, title: String) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 34.dp, bottom = 14.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun LyricRow(original: DemoLyricLine, translated: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text(original.text, style = JikanleTypography.cjk)
        original.transliteration?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text(translated, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
    }
    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
}

@Composable
private fun AlignmentNote(note: String) {
    Card(Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(stringResource(R.string.singability_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            Text(note, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun VocabularyCard(item: DemoVocabularyItem) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(item.term, style = JikanleTypography.cjk)
            item.reading?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(item.meaning, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
            item.explanation?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)) }
        }
    }
}
