package co.com.jikanle.feature.lesson

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.jikanle.R
import co.com.jikanle.core.design.theme.JikanleTypography

@Composable
fun LessonReaderScreen(
    onOpenEvents: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenSongbridge: () -> Unit,
    viewModel: LessonReaderViewModel = hiltViewModel(),
) {
    val lesson by viewModel.lesson.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val saveFailed by viewModel.saveFailed.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        val current = lesson
        when {
            current != null -> {
                val vocabById = current.vocabularyPicks.associateBy { it.id }
                val count = current.slideDeck.slides.size
                val step = progress.step.coerceIn(0, (count - 1).coerceAtLeast(0))
                key(step, progress.completed) {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(current.title, style = JikanleTypography.display)
                        current.description?.let { Muted(it) }
                        Muted(stringResource(R.string.lesson_languages, current.languageTarget.uppercase(), current.languageExplanation.uppercase()))
                        TextButton(onClick = onOpenSongbridge) { Text(stringResource(R.string.songbridge_open)) }
                        if (progress.completed) {
                            Text(stringResource(R.string.lesson_finished), style = MaterialTheme.typography.headlineSmall)
                            Body(stringResource(R.string.lesson_finished_body))
                            Button(onClick = onOpenEvents) { Text(stringResource(R.string.events_community)) }
                            OutlinedButton(onClick = onOpenFeedback) { Text(stringResource(R.string.beta_feedback)) }
                            TextButton(onClick = viewModel::restart, enabled = !saving) { Text(stringResource(R.string.lesson_restart)) }
                        } else if (count > 0) {
                            Text(stringResource(R.string.lesson_step, step + 1, count), style = MaterialTheme.typography.labelLarge)
                            LinearProgressIndicator(progress = { (step + 1f) / count }, modifier = Modifier.fillMaxWidth())
                            SlideCard(current.slideDeck.slides[step], vocabById, current.id == FUYU_LESSON_ID)
                        } else {
                            Body(stringResource(R.string.lesson_empty))
                        }
                    }
                }
                if (saveFailed) Text(stringResource(R.string.lesson_save_error), modifier = Modifier.padding(16.dp))
                if (!progress.completed && count > 0) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = viewModel::previous, enabled = step > 0 && !saving, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.lesson_previous))
                        }
                        Button(onClick = viewModel::next, enabled = !saving, modifier = Modifier.weight(1f)) {
                            Text(stringResource(if (step == count - 1) R.string.lesson_complete else R.string.lesson_next))
                        }
                    }
                }
            }
            refreshing -> CenteredNote(stringResource(R.string.lesson_loading))
            else -> Column(Modifier.padding(24.dp)) {
                Text(stringResource(R.string.lesson_empty))
                TextButton(onClick = viewModel::refresh) { Text(stringResource(R.string.retry)) }
            }
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
