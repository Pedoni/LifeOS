plugins {
    alias(libs.plugins.lifeos.android.library)
    alias(libs.plugins.lifeos.android.compose)
}

android {
    namespace = "it.edon.lifeos.core.ui"
}

dependencies {
    implementation(projects.core.designsystem)
}
