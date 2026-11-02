plugins {
    alias(libs.plugins.lifeos.android.library)
}

android {
    namespace = "it.edon.lifeos.core.network"
}

dependencies {
    implementation(projects.core.domain)
}
