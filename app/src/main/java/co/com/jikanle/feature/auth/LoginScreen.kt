package co.com.jikanle.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import co.com.jikanle.R
import co.com.jikanle.core.design.theme.JikanleTypography

@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.logo_mark), style = JikanleTypography.display, color = MaterialTheme.colorScheme.primary)
        Text(
            text = if (state.isSignUp) {
                stringResource(R.string.auth_join_title)
            } else {
                stringResource(R.string.auth_welcome_title)
            },
            style = JikanleTypography.display,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(R.string.auth_anchor_copy),
            style = JikanleTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp),
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(R.string.auth_email_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text(stringResource(R.string.auth_password_label)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        Button(
            onClick = viewModel::submit,
            enabled = !state.isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            if (state.isBusy) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    if (state.isSignUp) {
                        stringResource(R.string.auth_create_account)
                    } else {
                        stringResource(R.string.auth_sign_in)
                    },
                )
            }
        }

        TextButton(onClick = viewModel::toggleMode, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                if (state.isSignUp) {
                    stringResource(R.string.auth_existing_account)
                } else {
                    stringResource(R.string.auth_new_account)
                },
            )
        }

        state.messageRes?.let {
            Text(
                text = stringResource(it),
                style = JikanleTypography.body,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // OAuth opens a Custom Tab; the session returns via the jikanle://auth-callback deep
        // link. Requires Google enabled + configured in Supabase.
        OutlinedButton(
            onClick = viewModel::signInWithGoogle,
            enabled = !state.isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) { Text(stringResource(R.string.auth_continue_google)) }
    }
}
