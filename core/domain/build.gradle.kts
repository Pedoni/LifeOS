plugins {
    alias(libs.plugins.lifeos.kotlin.library)
}

dependencies {
    // Repository contracts expose Outcome and Flow.
    api(projects.core.common)
    api(libs.kotlinx.coroutines.core)
    // Only the annotation, so use cases can be constructor-injected; no Hilt or Android here.
    implementation(libs.javax.inject)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
