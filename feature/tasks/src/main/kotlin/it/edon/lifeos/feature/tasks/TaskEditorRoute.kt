package it.edon.lifeos.feature.tasks

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** The editor of one task, or of a new one when [taskId] is null. */
@Serializable
data class TaskEditorRoute(val taskId: String? = null)

/** Adds the task editor to a graph. [onBack] is called when the person leaves it or finishes. */
fun NavGraphBuilder.taskEditorDestination(onBack: () -> Unit) {
    composable<TaskEditorRoute> {
        TaskEditorScreen(onBack = onBack)
    }
}
