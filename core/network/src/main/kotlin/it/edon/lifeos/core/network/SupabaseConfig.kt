package it.edon.lifeos.core.network

/**
 * Where the Supabase project is. Only the public (anon) key may live in the app: data is
 * protected by Row Level Security, never by keeping this value secret. A `service_role` key
 * must never be placed here.
 */
data class SupabaseConfig(val url: String, val anonKey: String) {
    init {
        require(url.isNotBlank()) { "The Supabase URL is blank" }
        require(anonKey.isNotBlank()) { "The Supabase key is blank" }
    }

    // The key is kept out of logs and crash reports.
    override fun toString(): String = "SupabaseConfig(url=$url, anonKey=<redacted>)"
}
