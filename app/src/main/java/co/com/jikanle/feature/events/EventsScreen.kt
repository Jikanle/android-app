package co.com.jikanle.feature.events

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import co.com.jikanle.R
import co.com.jikanle.core.design.theme.JikanleTypography
import co.com.jikanle.core.domain.model.DiscoverableEvent
import co.com.jikanle.core.design.openWebLink
import co.com.jikanle.core.domain.repository.ProductEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventsScreen(
    onOpenLesson: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_wordmark), style = JikanleTypography.body, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = viewModel::refresh) { Icon(Icons.Filled.Refresh, stringResource(R.string.events_refresh)) }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.events_anchor), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.events_title), style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = {
                if (openWebLink(context, "https://luma.com/Jikanle?k=c")) viewModel.track(ProductEvent.EventRsvpOpened)
            }) { Text(stringResource(R.string.events_agenda)) }
            TextButton(onClick = onOpenLesson) { Text(stringResource(R.string.events_continue_lesson)) }
        }
        when (val current = state) {
            EventsUiState.Loading -> LoadingEvents()
            EventsUiState.Error -> ErrorEvents(viewModel::refresh)
            is EventsUiState.Content -> EventsList(current.events, current.refreshing, viewModel::track)
        }
    }
}

@Composable
private fun EventsList(events: List<DiscoverableEvent>, refreshing: Boolean, track: (ProductEvent) -> Unit) {
    if (events.isEmpty()) {
        EmptyEvents(refreshing)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(stringResource(R.string.events_subtitle), style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (refreshing) { Spacer(Modifier.height(8.dp)); CircularProgressIndicator(Modifier.width(18.dp), strokeWidth = 2.dp) }
        }
        items(events, key = { it.event.id }) { EventCard(it, track) }
    }
}

@Composable
private fun EventCard(item: DiscoverableEvent, track: (ProductEvent) -> Unit) {
    val context = LocalContext.current
    val event = item.event
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            event.coverImageUrl?.let { url -> AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxWidth().height(160.dp)) }
            item.organizationName?.let { Text(it, style = JikanleTypography.body, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
            Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                formatEventTime(event.startsAt, stringResource(R.string.events_time_tba)),
                style = JikanleTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            event.venueName?.let { Text(it, style = JikanleTypography.body) }
            event.summary?.takeIf { it.isNotBlank() }?.let { Text(it, style = JikanleTypography.body) }
                ?: event.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = JikanleTypography.body) }
            val label = listOfNotNull(event.language, event.level).joinToString(" · ")
            if (label.isNotBlank()) Text(label, style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                event.lumaUrl?.let { url -> Button(onClick = { if (openWebLink(context, url)) track(ProductEvent.EventRsvpOpened) }) { Icon(Icons.Filled.OpenInNew, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.events_luma)) } }
                if (event.startsAt != null) OutlinedButton(onClick = { addToCalendar(context, item) }) { Icon(Icons.Filled.CalendarMonth, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.events_calendar)) }
            }
            item.communityUrl?.let { url -> OutlinedButton(onClick = { if (openWebLink(context, url)) track(ProductEvent.CommunityOpened) }) { Icon(Icons.Filled.Groups, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.events_community)) } }
        }
    }
}

@Composable private fun LoadingEvents() = Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.events_loading)) }
@Composable private fun EmptyEvents(refreshing: Boolean) = Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { if (refreshing) CircularProgressIndicator() else Text(stringResource(R.string.events_empty), style = JikanleTypography.body) }
@Composable private fun ErrorEvents(retry: () -> Unit) = Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(stringResource(R.string.events_refresh_error), style = JikanleTypography.body); Spacer(Modifier.height(12.dp)); Button(onClick = retry) { Text(stringResource(R.string.retry)) } }

private fun formatEventTime(value: String?, fallback: String): String = if (value == null) fallback else runCatching { DateTimeFormatter.ofPattern("EEE d MMM · HH:mm z", Locale.getDefault()).withZone(ZoneId.of("America/Bogota")).format(Instant.parse(value)) }.getOrDefault(fallback)
private fun addToCalendar(context: Context, item: DiscoverableEvent) {
    val event = item.event
    val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.Events.TITLE, event.title).putExtra(CalendarContract.Events.EVENT_LOCATION, event.venueName ?: event.venueLocation).putExtra(CalendarContract.Events.DESCRIPTION, event.lumaUrl ?: event.summary ?: event.description)
    event.startsAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()?.let { start -> intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start) } }
    event.endsAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()?.let { end -> intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end) } }
    runCatching { context.startActivity(intent) }.onFailure {
        android.widget.Toast.makeText(context, R.string.link_unavailable, android.widget.Toast.LENGTH_LONG).show()
    }
}
