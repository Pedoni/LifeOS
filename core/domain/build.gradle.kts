plugins {
    alias(libs.plugins.lifeos.kotlin.library)
}

dependencies {
    // Repository contracts expose Outcome and Flow.
    api(projects.core.common)
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
