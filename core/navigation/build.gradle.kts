plugins {
    alias(libs.plugins.lifeos.android.library)
    alias(libs.plugins.lifeos.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "it.edon.lifeos.core.navigation"
}

dependencies {
    // Navigation Compose types appear in the public API (NavController extensions).
    api(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
}
