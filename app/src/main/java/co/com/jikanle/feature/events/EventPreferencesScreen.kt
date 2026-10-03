package co.com.jikanle.feature.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.jikanle.R

@Composable
fun EventPreferencesScreen(onSignIn: () -> Unit, viewModel: EventPreferencesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirm by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.event_preferences_title), style = MaterialTheme.typography.headlineSmall)
        if (!state.signedIn) {
            Button(onClick = onSignIn) { Text(stringResource(R.string.auth_sign_in)) }
        } else {
            Text(stringResource(R.string.event_preferences_scope))
            val editable = state.loaded && !state.busy
            val p = state.preferences
            PreferenceTags(R.string.event_languages, R.array.event_language_labels, listOf("ja", "pt", "es", "en", "ko", "zh"), p.languages, editable) { values -> viewModel.edit { it.copy(languages = values) } }
            PreferenceTags(R.string.event_interests, R.array.event_interest_labels, listOf("music", "culture", "technology", "literature"), p.interests, editable) { values -> viewModel.edit { it.copy(interests = values) } }
            PreferenceTags(R.string.event_domains, R.array.event_domain_labels, listOf("arts", "education", "engineering", "research"), p.domains, editable) { values -> viewModel.edit { it.copy(domains = values) } }
            PreferenceTags(R.string.event_goals, R.array.event_goal_labels, listOf("conversation", "culture", "music", "professional_exchange"), p.goals, editable) { values -> viewModel.edit { it.copy(goals = values) } }
            ConsentSwitch(R.string.event_matching_consent, p.matching, editable) { checked -> viewModel.edit { it.copy(matching = checked) } }
            ConsentSwitch(R.string.event_metrics_consent, p.metrics, editable) { checked -> viewModel.edit { it.copy(metrics = checked) } }
            Button(onClick = viewModel::save, enabled = editable) { Text(stringResource(R.string.event_preferences_save)) }
            TextButton(onClick = { confirm = true }, enabled = !state.busy) { Text(stringResource(R.string.event_preferences_withdraw)) }
            if (state.busy) CircularProgressIndicator()
            when (state.message) {
                PreferenceMessage.Saved -> Text(stringResource(R.string.event_preferences_saved))
                PreferenceMessage.Withdrawn -> Text(stringResource(R.string.event_preferences_withdrawn))
                PreferenceMessage.Failed -> {
                    Text(stringResource(R.string.event_preferences_failed))
                    if (!state.loaded) TextButton(onClick = viewModel::reload, enabled = !state.busy) { Text(stringResource(R.string.retry)) }
                }
                PreferenceMessage.None -> Unit
            }
        }
    }
    if (confirm && state.signedIn) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text(stringResource(R.string.event_preferences_withdraw)) },
        text = { Text(stringResource(R.string.event_preferences_withdraw_detail)) },
        confirmButton = { TextButton(onClick = { confirm = false; viewModel.withdraw() }) { Text(stringResource(R.string.event_preferences_withdraw)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.event_preferences_cancel)) } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferenceTags(title: Int, labels: Int, keys: List<String>, selected: List<String>, enabled: Boolean, change: (List<String>) -> Unit) {
    Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
    val names = stringArrayResource(labels)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.forEachIndexed { index, key ->
            FilterChip(selected = key in selected, enabled = enabled, label = { Text(names[index]) }, onClick = {
                change(if (key in selected) selected - key else selected + key)
            })
        }
    }
}

@Composable
private fun ConsentSwitch(label: Int, checked: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(label), Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = change, enabled = enabled)
    }
}
