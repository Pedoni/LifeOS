plugins {
    alias(libs.plugins.lifeos.kotlin.library)
}

dependencies {
    implementation(projects.core.common)
    // Repository contracts expose Flow.
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
