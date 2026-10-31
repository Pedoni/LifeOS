plugins {
    alias(libs.plugins.lifeos.android.library)
}

android {
    namespace = "it.edon.lifeos.core.database"
}

dependencies {
    implementation(projects.core.domain)
}
