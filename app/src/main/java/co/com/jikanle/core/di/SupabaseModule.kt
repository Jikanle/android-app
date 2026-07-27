package co.com.jikanle.core.di

import co.com.jikanle.BuildConfig
import co.com.jikanle.core.data.AppJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

/**
 * Builds the [SupabaseClient] from [BuildConfig] creds (read from local.properties) and
 * installs the plugins Jikanle uses. Creds are empty pre-backend, so the build stays green;
 * the client is only constructed when first injected, and nothing injects a repo until a
 * screen needs one — so an empty URL fails at runtime, not at build time.
 *
 * The plugin instances are also exposed individually so repositories can depend on just
 * [Postgrest] / [Storage] / [Auth] rather than the whole client.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        defaultSerializer = KotlinXSerializer(AppJson)
        install(Auth) {
            // OAuth redirect lands back here as jikanle://auth-callback — this scheme/host
            // pair MUST match the manifest intent-filter AND the Supabase dashboard
            // Auth → URL Configuration → Redirect URLs allowlist.
            scheme = "jikanle"
            host = "auth-callback"
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth = client.auth

    @Provides
    @Singleton
    fun provideStorage(client: SupabaseClient): Storage = client.storage

    @Provides
    @Singleton
    fun provideRealtime(client: SupabaseClient): Realtime = client.realtime
}
