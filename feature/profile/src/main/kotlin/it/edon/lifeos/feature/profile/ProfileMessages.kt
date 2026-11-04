package it.edon.lifeos.feature.profile

import androidx.annotation.StringRes
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.ThemeMode

// Nobody is signing in here, so the credential errors cannot occur and are reported as the
// generic one.
@StringRes
internal fun AuthError.messageRes(): Int = when (this) {
    AuthError.Network -> R.string.profile_error_network
    AuthError.RateLimited -> R.string.profile_error_rate_limited
    else -> R.string.profile_error_unknown
}

@StringRes
internal fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.profile_theme_system
    ThemeMode.LIGHT -> R.string.profile_theme_light
    ThemeMode.DARK -> R.string.profile_theme_dark
}
