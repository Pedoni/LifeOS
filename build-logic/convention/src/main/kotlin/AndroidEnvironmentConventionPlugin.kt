import com.android.build.api.dsl.ApplicationExtension
import java.io.StringReader
import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val PLACEHOLDER_URL = "https://placeholder.invalid"
private const val PLACEHOLDER_KEY = "placeholder-anon-key"

/**
 * Exposes the Supabase URL and public (anon) key as `BuildConfig` fields.
 *
 * Each value is read from `local.properties`, then from an environment variable (used by CI):
 *
 * | Property                | Environment variable      | Used by           |
 * |-------------------------|---------------------------|-------------------|
 * | `supabase.url`          | `SUPABASE_URL`            | release, debug    |
 * | `supabase.anonKey`      | `SUPABASE_ANON_KEY`       | release, debug    |
 * | `supabase.dev.url`      | `SUPABASE_DEV_URL`        | debug (optional)  |
 * | `supabase.dev.anonKey`  | `SUPABASE_DEV_ANON_KEY`   | debug (optional)  |
 *
 * Debug uses the `dev` values when present and falls back to the main ones, so a single
 * backend project can serve both until a second environment exists. A debug build without
 * any value still compiles (placeholders), so a clone or the CI needs no secret. A release
 * build without real values, or with a non-HTTPS URL, fails instead of shipping placeholders.
 */
class AndroidEnvironmentConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val localProperties = readLocalProperties()

            fun setting(property: String, variable: String): String? =
                localProperties.getProperty(property)?.takeIf { it.isNotBlank() }
                    ?: providers.environmentVariable(variable).orNull?.takeIf { it.isNotBlank() }

            val releaseUrl = setting("supabase.url", "SUPABASE_URL")
            val releaseKey = setting("supabase.anonKey", "SUPABASE_ANON_KEY")
            val debugUrl = setting("supabase.dev.url", "SUPABASE_DEV_URL") ?: releaseUrl
            val debugKey = setting("supabase.dev.anonKey", "SUPABASE_DEV_ANON_KEY") ?: releaseKey

            if (debugUrl == null || debugKey == null) {
                logger.warn(
                    "Supabase is not configured: debug builds use placeholders and cannot " +
                        "reach a backend. Set supabase.url and supabase.anonKey in local.properties."
                )
            }

            val releaseProblem = when {
                releaseUrl == null || releaseKey == null ->
                    "supabase.url and supabase.anonKey are required for a release build " +
                        "(local.properties or SUPABASE_URL / SUPABASE_ANON_KEY)."

                !releaseUrl.startsWith("https://") ->
                    "supabase.url must use https for a release build."

                else -> null
            }

            extensions.configure<ApplicationExtension> {
                buildFeatures {
                    buildConfig = true
                }
                buildTypes {
                    getByName("debug") {
                        buildConfigField("String", "SUPABASE_URL", (debugUrl ?: PLACEHOLDER_URL).quoted())
                        buildConfigField("String", "SUPABASE_ANON_KEY", (debugKey ?: PLACEHOLDER_KEY).quoted())
                    }
                    getByName("release") {
                        buildConfigField("String", "SUPABASE_URL", (releaseUrl ?: "").quoted())
                        buildConfigField("String", "SUPABASE_ANON_KEY", (releaseKey ?: "").quoted())
                    }
                }
            }

            if (releaseProblem != null) {
                tasks.matching { it.name == "preReleaseBuild" }.configureEach {
                    doFirst { throw GradleException(releaseProblem) }
                }
            }
        }
    }
}

private fun Project.readLocalProperties(): Properties {
    val text = providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .orNull
    return Properties().apply { if (text != null) load(StringReader(text)) }
}

private fun String.quoted(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
