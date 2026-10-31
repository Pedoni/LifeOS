plugins {
    alias(libs.plugins.lifeos.android.library)
}

android {
    namespace = "it.edon.lifeos.core.data"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.network)
}
