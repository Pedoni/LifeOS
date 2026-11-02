plugins {
    alias(libs.plugins.lifeos.android.library)
    alias(libs.plugins.lifeos.android.room)
    alias(libs.plugins.lifeos.android.hilt)
}

android {
    namespace = "it.edon.lifeos.core.database"
}

dependencies {
    // DAOs expose Flow in their public API.
    api(libs.kotlinx.coroutines.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
