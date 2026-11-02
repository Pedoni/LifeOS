package it.edon.lifeos.core.navigation

import kotlinx.serialization.Serializable

// Type-safe routes of the top-level destinations (the tabs of the bottom bar).
// Feature modules will define their own nested routes next to their screens.

@Serializable
data object HomeRoute

@Serializable
data object TasksRoute

@Serializable
data object CalendarRoute

@Serializable
data object HabitsRoute

@Serializable
data object MoreRoute
