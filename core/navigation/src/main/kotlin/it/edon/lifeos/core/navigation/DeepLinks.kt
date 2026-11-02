package it.edon.lifeos.core.navigation

/** URI scheme handled by the app, declared in the manifest. */
const val DEEP_LINK_SCHEME = "lifeos"

/** Deep link of a top-level destination, for example `lifeos://tasks`. */
val TopLevelDestination.deepLink: String
    get() = "$DEEP_LINK_SCHEME://$deepLinkPath"
