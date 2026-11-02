plugins {
    alias(libs.plugins.lifeos.android.library)
    alias(libs.plugins.lifeos.android.hilt)
}

android {
    namespace = "it.edon.lifeos.core.data"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.network)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
