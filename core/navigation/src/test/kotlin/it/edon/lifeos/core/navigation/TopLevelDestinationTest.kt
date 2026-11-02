package it.edon.lifeos.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelDestinationTest {
    @Test
    fun everyDestinationHasItsOwnDeepLink() {
        val links = TopLevelDestination.entries.map { it.deepLink }

        assertEquals(links.size, links.toSet().size)
    }

    @Test
    fun deepLinksUseTheAppScheme() {
        assertTrue(
            TopLevelDestination.entries.all {
                it.deepLink.startsWith("$DEEP_LINK_SCHEME://")
            }
        )
    }

    @Test
    fun everyDestinationHasItsOwnRoute() {
        val routes = TopLevelDestination.entries.map { it.route::class }

        assertEquals(routes.size, routes.toSet().size)
    }
}
