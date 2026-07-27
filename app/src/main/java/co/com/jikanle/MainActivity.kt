package co.com.jikanle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import co.com.jikanle.core.design.theme.JikanleTheme
import co.com.jikanle.navigation.JikanleNavGraph
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabaseClient: Lazy<SupabaseClient>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // The launch intent may already carry the OAuth callback (jikanle://auth-callback).
        handleSupabaseDeeplink(intent)
        setContent {
            JikanleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    JikanleNavGraph()
                }
            }
        }
    }

    /** OAuth Custom Tab returns here (singleTop) — hand the callback URL to Supabase. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSupabaseDeeplink(intent)
    }

    private fun handleSupabaseDeeplink(intent: Intent) {
        if (BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            supabaseClient.get().handleDeeplinks(intent)
        }
    }
}
