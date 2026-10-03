package co.com.jikanle.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.jikanle.BuildConfig
import co.com.jikanle.R
import co.com.jikanle.core.design.theme.JikanleTypography
import co.com.jikanle.core.domain.repository.AuthState

@Composable
fun BetaScreen(onSignIn: () -> Unit, onEventPreferences: () -> Unit = {}, viewModel: BetaViewModel = hiltViewModel()) {
    val auth by viewModel.authState.collectAsStateWithLifecycle()
    val sharing by viewModel.sharing.collectAsStateWithLifecycle()
    val delivery by viewModel.delivery.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    var rating by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.beta_title), style = JikanleTypography.display)
        Text(stringResource(R.string.beta_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
        TextButton(onClick = onEventPreferences) { Text(stringResource(R.string.event_preferences_title)) }
        if (auth !is AuthState.Authenticated) {
            Text(stringResource(R.string.beta_sign_in_note))
            Button(onClick = onSignIn) { Text(stringResource(R.string.auth_sign_in)) }
        } else {
            Text(stringResource(R.string.beta_signed_in), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.beta_share), modifier = Modifier.weight(1f))
                Switch(checked = sharing, onCheckedChange = viewModel::share, enabled = !busy)
            }
            Text(stringResource(R.string.beta_privacy), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.beta_question), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.beta_rating_scale), style = MaterialTheme.typography.bodySmall)
            Column {
                (1..5).forEach { value ->
                    FilterChip(
                        selected = rating == value,
                        onClick = { rating = value },
                        label = { Text(stringResource(R.string.beta_rating, value)) },
                        enabled = delivery != Delivery.Sending && delivery != Delivery.Pending,
                    )
                }
            }
            Button(onClick = { viewModel.submit(rating) }, enabled = rating > 0 && delivery != Delivery.Sending && delivery != Delivery.Pending && delivery != Delivery.Sent) {
                Text(stringResource(R.string.beta_send))
            }
            when (delivery) {
                Delivery.Sent -> Text(stringResource(R.string.beta_sent))
                Delivery.Pending -> {
                    Text(stringResource(R.string.beta_pending))
                    TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.retry)) }
                }
                Delivery.Failed -> Text(stringResource(R.string.beta_failed))
                Delivery.Sending -> Text(stringResource(R.string.beta_sending))
                Delivery.Idle -> Unit
            }
            TextButton(onClick = viewModel::signOut) { Text(stringResource(R.string.sign_out)) }
        }
    }
}
