package co.com.jikanle.feature.lesson

import co.com.jikanle.core.design.openWebLink
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
internal fun SlideCard(slide: Slide, vocabById: Map<String?, Vocabulary>, hasExternalSource: Boolean) {
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
                    ExternalListenButton(hasExternalSource)
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
                    ExternalListenButton(hasExternalSource)
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
internal fun Label(text: String) =
    Text(text, style = JikanleTypography.body, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

@Composable
private fun AdaptiveText(text: String) =
    Text(
        text = text,
        style = if (hasCJK(text)) JikanleTypography.cjk else JikanleTypography.body,
        color = MaterialTheme.colorScheme.onSurface,
    )

@Composable
internal fun Body(text: String) =
    Text(text, style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurface)

@Composable
internal fun Muted(text: String) =
    Text(text, style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun ExternalListenButton(hasExternalSource: Boolean) {
    val context = LocalContext.current
    if (hasExternalSource) {
        OutlinedButton(onClick = { openWebLink(context, "https://open.spotify.com/search/Fuyu%20no%20Hanashi") }) {
            Text(stringResource(R.string.lesson_external_listen))
        }
        Muted(stringResource(R.string.lesson_external_return))
    } else {
        Muted(stringResource(R.string.lesson_audio_unavailable))
    }
}
