package co.com.jikanle.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.com.jikanle.R
import co.com.jikanle.core.design.theme.JikanleTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(stringResource(R.string.profile_anchor), style = JikanleTypography.body, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.profile_title), style = JikanleTypography.display, color = MaterialTheme.colorScheme.onBackground)
        Text(stringResource(R.string.profile_body), style = JikanleTypography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            stringArrayResource(R.array.profile_hobby_tags).forEach { tag ->
                AssistChip(onClick = {}, label = { Text(tag) })
            }
        }
    }
}
