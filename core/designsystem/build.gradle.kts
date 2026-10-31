plugins {
    alias(libs.plugins.lifeos.android.library)
    alias(libs.plugins.lifeos.android.compose)
}

android {
    namespace = "it.edon.lifeos.core.designsystem"
}

dependencies {
    // The public API exposes Material 3 types (ColorScheme, Typography, Shapes).
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
