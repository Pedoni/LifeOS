package it.edon.lifeos.core.data.mapper

import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.network.auth.RemoteAuthException
import it.edon.lifeos.core.network.auth.RemoteSessionState
import it.edon.lifeos.core.network.auth.RemoteUser

internal fun RemoteUser.toDomain() = AuthUser(id = id, email = email, displayName = displayName)

internal fun RemoteSessionState.toDomain(): AuthState = when (this) {
    RemoteSessionState.Initializing -> AuthState.Loading
    RemoteSessionState.SignedOut -> AuthState.SignedOut
    is RemoteSessionState.SignedIn -> AuthState.SignedIn(user.toDomain())
}

internal fun RemoteAuthException.toDomain(): AuthError = when (this) {
    is RemoteAuthException.InvalidCredentials -> AuthError.InvalidCredentials
    is RemoteAuthException.EmailAlreadyRegistered -> AuthError.EmailAlreadyRegistered
    is RemoteAuthException.WeakPassword -> AuthError.WeakPassword
    is RemoteAuthException.EmailNotConfirmed -> AuthError.EmailNotConfirmed
    is RemoteAuthException.InvalidInput -> AuthError.InvalidInput
    is RemoteAuthException.RateLimited -> AuthError.RateLimited
    is RemoteAuthException.Network -> AuthError.Network
    is RemoteAuthException.Unknown -> AuthError.Unknown
}
