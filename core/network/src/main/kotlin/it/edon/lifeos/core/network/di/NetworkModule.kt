package it.edon.lifeos.core.network.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import it.edon.lifeos.core.network.SupabaseConfig
import it.edon.lifeos.core.network.auth.AuthRemoteDataSource
import it.edon.lifeos.core.network.auth.SupabaseAuthDataSource
import it.edon.lifeos.core.network.session.EncryptedSessionManager
import it.edon.lifeos.core.network.session.KeystoreSessionCipher
import java.io.File
import javax.inject.Singleton

private const val SESSION_FILE = "auth/session.bin"

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun provideSupabaseClient(
        config: SupabaseConfig,
        @ApplicationContext context: Context
    ): SupabaseClient = createSupabaseClient(
        supabaseUrl = config.url,
        supabaseKey = config.anonKey
    ) {
        install(Auth) {
            // noBackupFilesDir is excluded from cloud and device-transfer backups, so a session
            // is never restored onto another device.
            sessionManager = EncryptedSessionManager(
                file = File(context.noBackupFilesDir, SESSION_FILE),
                cipher = KeystoreSessionCipher()
            )
        }
        install(Postgrest)
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth = client.auth

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(auth: Auth, postgrest: Postgrest): AuthRemoteDataSource =
        SupabaseAuthDataSource(auth, postgrest)
}
