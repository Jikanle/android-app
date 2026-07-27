package co.com.jikanle.feature.lesson

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.jikanle.R
import co.com.jikanle.core.design.theme.JikanleTypography
import co.com.jikanle.core.design.theme.hasCJK
import co.com.jikanle.core.domain.model.CulturalSlide
import co.com.jikanle.core.domain.model.DiscussionSlide
import co.com.jikanle.core.domain.model.GrammarNoteSlide
import co.com.jikanle.core.domain.model.IntroSlide
import co.com.jikanle.core.domain.model.ListenSlide
import co.com.jikanle.core.domain.model.LyricFocusSlide
import co.com.jikanle.core.domain.model.OutroSlide
import co.com.jikanle.core.domain.model.SecondListenSlide
import co.com.jikanle.core.domain.model.Slide
import co.com.jikanle.core.domain.model.Vocabulary
import co.com.jikanle.core.domain.model.VocabularySlide

@Composable
fun LessonReaderScreen(
    onOpenAuth: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    viewModel: LessonReaderViewModel = hiltViewModel(),
) {
    val lesson by viewModel.lesson.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.app_wordmark), style = JikanleTypography.body, color = MaterialTheme.colorScheme.primary)
            Row {
                TextButton(onClick = onOpenProfile) { Text(stringResource(R.string.profile_nav)) }
                TextButton(onClick = onOpenAuth) { Text(stringResource(R.string.auth_nav)) }
                TextButton(onClick = viewModel::signOut) { Text(stringResource(R.string.sign_out)) }
            }
        }
        HorizontalDivider()

        val current = lesson
        when {
            current != null -> {
                val vocabById = current.vocabularyPicks.associateBy { it.id }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        Text(current.title, style = JikanleTypography.display, color = MaterialTheme.colorScheme.onBackground)
                        current.description?.let {
                            Text(it, style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                    items(current.slideDeck.slides) { slide -> SlideCard(slide, vocabById) }
                }
            }
            refreshing -> CenteredNote(stringResource(R.string.lesson_loading))
            else -> CenteredNote(stringResource(R.string.lesson_empty))
        }
    }
}

@Composable
private fun CenteredNote(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text, style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SlideCard(slide: Slide, vocabById: Map<String?, Vocabulary>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            when (slide) {
                is IntroSlide -> {
                    Label(stringResource(R.string.slide_label_intro))
                    AdaptiveText(slide.title)
                    slide.subtitle?.let { Body(it) }
                    slide.notes?.let { Muted(it) }
                }
                is ListenSlide -> {
                    Label(stringResource(R.string.slide_label_first_listen))
                    slide.firstListenInstruction?.let { Body(it) }
                    PlayStubButton(stringResource(R.string.lesson_first_listen_play))
                }
                is VocabularySlide -> {
                    Label(stringResource(R.string.slide_label_vocabulary))
                    slide.items.forEach { id ->
                        val v = vocabById[id]
                        if (v != null) {
                            AdaptiveText("${v.term}  ${v.reading ?: ""}")
                            Muted("${v.meaning}${v.example?.let { " · $it" } ?: ""}")
                        }
                    }
                }
                is GrammarNoteSlide -> {
                    Label(stringResource(R.string.slide_label_grammar))
                    AdaptiveText(slide.pattern)
                    slide.explanationMd?.let { Body(it) }
                    slide.examples.forEach { Muted("· $it") }
                }
                is LyricFocusSlide -> {
                    Label(stringResource(R.string.slide_label_lyric_focus))
                    slide.lyricRange?.let {
                        Muted(stringResource(R.string.lesson_seconds_range, it.startMs / 1000, it.endMs / 1000))
                    }
                    slide.explanationMd?.let { Body(it) }
                }
                is CulturalSlide -> {
                    Label(stringResource(R.string.slide_label_cultural))
                    AdaptiveText(slide.title)
                    slide.bodyMd?.let { Body(it) }
                }
                is DiscussionSlide -> {
                    Label(stringResource(R.string.slide_label_discussion))
                    slide.prompts.forEach { Body(it) }
                }
                is SecondListenSlide -> {
                    Label(stringResource(R.string.slide_label_second_listen))
                    Body(
                        if (slide.withLyrics) {
                            stringResource(R.string.lesson_second_listen_with_lyrics)
                        } else {
                            stringResource(R.string.lesson_second_listen_without_lyrics)
                        },
                    )
                    PlayStubButton(stringResource(R.string.lesson_second_listen_play))
                }
                is OutroSlide -> {
                    Label(stringResource(R.string.slide_label_outro))
                    slide.nextStepsMd?.let { Body(it) }
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) =
    Text(text, style = JikanleTypography.body, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

@Composable
private fun AdaptiveText(text: String) =
    Text(
        text = text,
        style = if (hasCJK(text)) JikanleTypography.cjk else JikanleTypography.body,
        color = MaterialTheme.colorScheme.onSurface,
    )

@Composable
private fun Body(text: String) =
    Text(text, style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurface)

@Composable
private fun Muted(text: String) =
    Text(text, style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun PlayStubButton(text: String) {
    Button(
        onClick = { Log.d("JikanleLesson", "Audio playback placeholder tapped") },
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Text(text, modifier = Modifier.padding(start = 6.dp))
    }
}
