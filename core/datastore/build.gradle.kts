plugins {
    alias(libs.plugins.lifeos.android.library)
}

android {
    namespace = "it.edon.lifeos.core.datastore"
}

dependencies {
    implementation(projects.core.domain)
}
